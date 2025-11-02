package com.catowl.sts.service.impl;

import com.catowl.sts.exception.BadRequestException;
import com.catowl.sts.exception.InternetServerException;
import com.catowl.sts.mapper.ReportMapper;
import com.catowl.sts.model.DTO.Request.ReportGenerateRequest;
import com.catowl.sts.model.DTO.Response.ReportResponse;
import com.catowl.sts.model.entity.AnalysisReport;
import com.catowl.sts.model.entity.User;
import com.catowl.sts.model.entity.WaterQualityData;
import com.catowl.sts.model.entity.WaterSource;
import com.catowl.sts.service.ReportService;
import com.catowl.sts.utils.MyQrCodeUtil;
import com.catowl.sts.utils.RedisCache;
import de.huxhorn.sulky.ulid.ULID;
import io.github.imfangs.dify.client.DifyChatflowClient;
import io.github.imfangs.dify.client.enums.ResponseMode;
import io.github.imfangs.dify.client.exception.DifyApiException;
import io.github.imfangs.dify.client.model.chat.ChatMessage;
import io.github.imfangs.dify.client.model.chat.ChatMessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    // --- Redis Key 常量 ---
    private static final String RATE_LIMIT_KEY_PREFIX = "ratelimit:report:user:";
    private static final String LOCK_KEY_PREFIX = "lock:report:source:";

    // --- 业务常量 ---
    private static final int MAX_REPORT_ATTEMPTS = 3; // 10分钟内最大尝试次数
    private static final long RATE_LIMIT_WINDOW_MIN = 10; // 10分钟窗口
    private static final long LOCK_TIMEOUT_SEC = 60; // 锁超时时间60秒，防止死锁
    private static final int RECENT_DATA_LIMIT = 20; // 查询最近20条数据

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private DifyChatflowClient chatflowClient;

    @Value("${file.qrcode-dir}")
    private String qrDir;

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    @Override
    @Transactional
    public ReportResponse generateReport(ReportGenerateRequest request){
        // 限流
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        checkRateLimit(user.getId());

        String sourceStrId = request.getSourceStrId();
        String lockKey = LOCK_KEY_PREFIX + sourceStrId;

        // 分布式锁
        Boolean acquired = redisCache.redisTemplate.opsForValue().setIfAbsent(lockKey,user.getId().toString(),LOCK_TIMEOUT_SEC, TimeUnit.SECONDS);

        if(acquired == null || !acquired) {
            throw new BadRequestException("报告正在生成中，请勿重复提交");
        }

        // 获取业务数据
        WaterSource waterSource = reportMapper.findWaterSourceByStrId(sourceStrId);
        if(waterSource == null || !acquired) {
            throw new BadRequestException("该用户不存在该水源");
        }
        List<WaterQualityData> dataList = reportMapper.findRecentDataBySourceId(waterSource.getId(),RECENT_DATA_LIMIT);
        if(dataList == null || dataList.isEmpty()) {
            throw new BadRequestException("该水源近期无水质数据，无法生成报告");
        }

        String formattedData = formatDataForDify(dataList);
        ChatMessage message = buildDifyRequest(waterSource,formattedData,user);
        try {
            ChatMessageResponse response = chatflowClient.sendChatMessage(message);
            String reportContent = response.getAnswer();
            String difyMessageId = response.getMessageId();
            AnalysisReport report = saveReportToDb(reportContent,difyMessageId,waterSource.getId(),dataList);
            return convertToResponseDTO(report,waterSource.getStrId());
        } catch (IOException | DifyApiException e) {
            throw new InternetServerException("服务器异常，请将错误信息提交给管理员");
        } finally {
            redisCache.deleteObject(lockKey);
        }
    }

    //限流函数
    private void checkRateLimit(Long userId){
        String rateLimitKey = RATE_LIMIT_KEY_PREFIX + userId;
        Long currentCount = redisCache.redisTemplate.opsForValue().increment(rateLimitKey);
        if(currentCount == null){
            throw new InternetServerException("redis服务异常");
        }
        if(currentCount == 1){
            redisCache.expire(rateLimitKey,RATE_LIMIT_WINDOW_MIN,TimeUnit.MINUTES);
        }
        if(currentCount > MAX_REPORT_ATTEMPTS) {
            throw new BadRequestException("操作过于频繁，请10分钟后再试");
        }
    }

    //格式化数据库数据提交给dify
    private String formatDataForDify(List<WaterQualityData> dataList){
        //返回格式："150.1, 12.2, 812"
        return dataList.stream()
                .map(data->data.getTurbidityValue().toString())
                .collect(Collectors.joining(", "));
    }

    private ChatMessage buildDifyRequest(WaterSource waterSource,String formattedData, User user){
        return ChatMessage.builder()
                .query("请根据最新数据生成分析报告")
                .user(user.getStrId())
                .inputs(Map.of("recent_turbidity_data", formattedData))
                .conversationId(waterSource.getDifyConversationId())
                .responseMode(ResponseMode.BLOCKING)
                .build();
    }

    //保存报告到数据库并生成二维码
    @Transactional
    public AnalysisReport saveReportToDb(String reportContent, String difyMessageId, Long sourceId, List<WaterQualityData> dataList){
        AnalysisReport report = new AnalysisReport();
        ULID ulidGenerator = new ULID();
        ULID.Value ulid = ulidGenerator.nextValue();
        report.setStrId(ulid.toString());
        report.setSourceId(sourceId);
        report.setReportContent(reportContent);
        report.setDifyMessageId(difyMessageId);
        report.setPublished(false);
        report.setGeneratedAt(LocalDateTime.now());

        //生成二维码
        File reportDir = new File(qrDir + File.separator + report.getStrId());
        String fileName = difyMessageId + ".png";
        String contentUrl = frontendBaseUrl + "/reports/" + report.getStrId();
        MyQrCodeUtil.createCodeToFile(reportContent, reportDir,fileName);

        String urlPath = "/qrcode/" + report.getStrId() + "/" +fileName;
        report.setQrCodePath(urlPath);
        reportMapper.insertReport(report);
        List<Long> dataIds = dataList.stream().map(WaterQualityData::getId).collect(Collectors.toList());
        reportMapper.insertReportDataLink(report.getId(),dataIds);
        return report;
    }

    //把saveReportToDb返回的报告包装成DTO
    private ReportResponse convertToResponseDTO(AnalysisReport report, String sourceStrId){
        ReportResponse dto = new ReportResponse();
        dto.setStrId(report.getStrId());
        dto.setSourceStrId(sourceStrId);
        dto.setReportContent(report.getReportContent());
        dto.setQrCodePath(report.getQrCodePath());
        dto.setGeneratedAt(report.getGeneratedAt());
        dto.setPublished(report.isPublished());
        return dto;
    }
}
