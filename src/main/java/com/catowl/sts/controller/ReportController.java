package com.catowl.sts.controller;

import com.catowl.sts.model.DTO.Request.ReportGenerateRequest;
import com.catowl.sts.model.DTO.Response.MyApiResponse;
import com.catowl.sts.model.DTO.Response.ReportResponse;
import com.catowl.sts.model.DTO.Response.ReportStatusUpdateResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Api(tags = "4. 分析报告 (私有)", description = "管理用户的分析报告、生成、发布和删除")
public class ReportController {
    @PostMapping("/generate")
    @ApiOperation(value = "生成新报告", notes = "请求 Dify 工作流为指定水源生成新报告")
    @ApiResponses({
            @io.swagger.annotations.ApiResponse(code = 201, message = "报告生成成功")
    })
    public ResponseEntity<MyApiResponse<ReportResponse>> generateReport(
            @Valid @RequestBody ReportGenerateRequest generateRequest) {

        // --- Mock Data ---
        ReportResponse mockReport = new ReportResponse();
        mockReport.setStrId("report_ulid_mock_qwe");
        mockReport.setSourceStrId(generateRequest.getSourceStrId());
        mockReport.setReportContent("## 模拟水质分析报告\n\n- **水源类型**: 工业\n- **近期浊度**: 平均 155.0 NTU\n- **分析**: 浊度偏高，建议启动过滤程序。\n");
        mockReport.setQrCodePath("/static/qr/report_ulid_mock_qwe.png");
        mockReport.setGeneratedAt(LocalDateTime.now());
        mockReport.setDifyMessageId("dify_msg_mock_1a2b3c");
        mockReport.setPublished(false); // 默认未发布

        MyApiResponse<ReportResponse> response = new MyApiResponse<>("报告生成成功", mockReport);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @ApiOperation(value = "获取当前用户的所有报告", notes = "列出当前用户的所有报告 (包括未发布的)")
    public ResponseEntity<MyApiResponse<List<ReportResponse>>> getAllReports() {

        // --- Mock Data ---
        ReportResponse mockReport = new ReportResponse();
        mockReport.setStrId("report_ulid_mock_qwe");
        mockReport.setSourceStrId("source_ulid_mock_abc");
        mockReport.setReportContent("## 模拟水质分析报告...");
        mockReport.setQrCodePath("/static/qr/report_ulid_mock_qwe.png");
        mockReport.setGeneratedAt(LocalDateTime.now().minusDays(1));
        mockReport.setDifyMessageId("dify_msg_mock_1a2b3c");
        mockReport.setPublished(false);

        List<ReportResponse> mockList = Collections.singletonList(mockReport);
        MyApiResponse<List<ReportResponse>> response = new MyApiResponse<>("获取成功", mockList);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{reportStrId}")
    @ApiOperation(value = "获取特定报告详情 (私有)", notes = "获取报告详情，无论是否发布 (需要所有权)")
    public ResponseEntity<MyApiResponse<ReportResponse>> getReportById(
            @ApiParam(value = "报告的字符串ID", example = "report_ulid_mock_qwe")
            @PathVariable String reportStrId) {

        // --- Mock Data ---
        ReportResponse mockReport = new ReportResponse();
        mockReport.setStrId(reportStrId);
        mockReport.setSourceStrId("source_ulid_mock_abc");
        mockReport.setReportContent("## 模拟水质分析报告\n\n- **水源类型**: 工业\n- **近期浊度**: 平均 155.0 NTU\n- **分析**: 浊度偏高，建议启动过滤程序。\n");
        mockReport.setQrCodePath("/static/qr/" + reportStrId + ".png");
        mockReport.setGeneratedAt(LocalDateTime.now().minusDays(1));
        mockReport.setDifyMessageId("dify_msg_mock_1a2b3c");
        mockReport.setPublished(false);

        MyApiResponse<ReportResponse> response = new MyApiResponse<>("获取成功", mockReport);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reportStrId}")
    @ApiOperation(value = "删除报告", notes = "删除一份分析报告")
    public ResponseEntity<MyApiResponse<String>> deleteReport(
            @ApiParam(value = "报告的字符串ID", example = "report_ulid_mock_qwe")
            @PathVariable String reportStrId) {

        // --- Mock Logic ---
        MyApiResponse<String> response = new MyApiResponse<>("删除成功", reportStrId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{reportStrId}/publish")
    @ApiOperation(value = "发布报告", notes = "将报告设为公开可见 (二维码将生效)")
    public ResponseEntity<MyApiResponse<ReportStatusUpdateResponse>> publishReport(
            @ApiParam(value = "报告的字符串ID", example = "report_ulid_mock_qwe")
            @PathVariable String reportStrId) {

        // --- Mock Data ---
        ReportStatusUpdateResponse statusResponse = new ReportStatusUpdateResponse(reportStrId, true);
        MyApiResponse<ReportStatusUpdateResponse> response = new MyApiResponse<>("报告已发布", statusResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{reportStrId}/unpublish")
    @ApiOperation(value = "取消发布报告", notes = "将报告设为私有 (二维码将失效)")
    public ResponseEntity<MyApiResponse<ReportStatusUpdateResponse>> unpublishReport(
            @ApiParam(value = "报告的字符串ID", example = "report_ulid_mock_qwe")
            @PathVariable String reportStrId) {

        // --- Mock Data ---
        ReportStatusUpdateResponse statusResponse = new ReportStatusUpdateResponse(reportStrId, false);
        MyApiResponse<ReportStatusUpdateResponse> response = new MyApiResponse<>("报告已设为私有", statusResponse);
        return ResponseEntity.ok(response);
    }
}
