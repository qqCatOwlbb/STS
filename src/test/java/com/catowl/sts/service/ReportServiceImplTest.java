package com.catowl.sts.service;

import com.catowl.sts.exception.BadRequestException;
import com.catowl.sts.mapper.ReportMapper;
import com.catowl.sts.model.dto.Request.ReportGenerateRequest;
import com.catowl.sts.model.dto.Response.ReportResponse;
import com.catowl.sts.model.entity.AnalysisReport;
import com.catowl.sts.model.entity.User;
import com.catowl.sts.model.entity.WaterQualityData;
import com.catowl.sts.model.entity.WaterSource;
import com.catowl.sts.service.impl.ReportServiceImpl;
import com.catowl.sts.utils.RedisCache;
import io.github.imfangs.dify.client.DifyChatflowClient;
import io.github.imfangs.dify.client.model.chat.ChatMessage;
import io.github.imfangs.dify.client.model.chat.ChatMessageResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
/**
 * @program: STS
 * @description:
 * @author: qqCatOwlbb
 * @create: 2025-12-21 16:41
 **/
@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportMapper reportMapper;
    @Mock
    private RedisCache redisCache;
    @Mock
    private DifyChatflowClient chatflowClient;
    @Mock
    private RedisTemplate redisTemplate; // RedisCache 内部依赖
    @Mock
    private ValueOperations valueOperations; // Redis 操作

    @InjectMocks
    private ReportServiceImpl reportService;

    private MockedStatic<SecurityContextHolder> securityContextMock;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // 注入 RedisTemplate 到 RedisCache (因为是 Autowired 字段注入)
        ReflectionTestUtils.setField(redisCache, "redisTemplate", redisTemplate);

        // 模拟 Redis 操作
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 模拟 Security Context
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setStrId("user_123");

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        securityContextMock = mockStatic(SecurityContextHolder.class);
        securityContextMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUser);
    }

    @AfterEach
    void tearDown() {
        securityContextMock.close();
    }

    @Test
    void generateReport_ShouldSucceed_WhenLockAcquiredAndDataExists() throws Exception {
        // Arrange
        ReportGenerateRequest request = new ReportGenerateRequest();
        request.setSourceStrId("source_abc");

        WaterSource mockSource = new WaterSource();
        mockSource.setId(10L);
        mockSource.setSourceName("Test Source");
        mockSource.setDifyConversationId("conv_123");
        mockSource.setSourceType("河流");
        mockSource.setDescription("这是一个测试水源");

        WaterQualityData mockData = new WaterQualityData();
        mockData.setId(100L);
        mockData.setTurbidityValue(new BigDecimal("10.5"));

        // 1. 模拟限流计数
        when(valueOperations.increment(anyString())).thenReturn(1L);
        // 2. 模拟分布式锁获取成功
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        // 3. 模拟数据库查询
        when(reportMapper.findWaterSourceByStrId("source_abc")).thenReturn(mockSource);
        when(reportMapper.findRecentDataBySourceId(eq(10L), anyInt())).thenReturn(Collections.singletonList(mockData));
        // 4. 模拟 Dify 返回
        ChatMessageResponse difyResponse = new ChatMessageResponse();
        difyResponse.setAnswer("AI Analysis Result");
        difyResponse.setMessageId("msg_dify_123");
        when(chatflowClient.sendChatMessage(any(ChatMessage.class))).thenReturn(difyResponse);

        // Act
        ReportResponse response = reportService.generateReport(request);

        // Assert
        assertNotNull(response);
        assertEquals("AI Analysis Result", response.getReportContent());
        assertNotNull(response.getStrId()); // 验证生成了新的 ID

        // 验证数据库插入
        verify(reportMapper).insertReport(any(AnalysisReport.class));
        verify(reportMapper).insertReportDataLink(any(), anyList());

        // 验证锁被释放
        verify(redisCache).deleteObject(contains("lock:report:source:"));
    }

    @Test
    void generateReport_ShouldFail_WhenLocked() {
        // Arrange
        ReportGenerateRequest request = new ReportGenerateRequest();
        request.setSourceStrId("source_abc");

        when(valueOperations.increment(anyString())).thenReturn(1L);
        // 模拟锁已被占用 (返回 false)
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> reportService.generateReport(request));
    }
}
