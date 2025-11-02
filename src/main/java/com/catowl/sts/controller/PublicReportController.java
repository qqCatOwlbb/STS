package com.catowl.sts.controller;

import com.catowl.sts.model.DTO.Response.MyApiResponse;
import com.catowl.sts.model.DTO.Response.ReportPublicResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/public/reports")
@Api(tags = "5. 分析报告 (公开)", description = "供任何人 (例如通过二维码) 查看已发布的报告")
public class PublicReportController {

    @GetMapping("/{reportStrId}")
    @ApiOperation(value = "查看公开报告", notes = "根据 strId 获取已发布的报告详情 (无需认证)")
    @ApiResponses({
            @io.swagger.annotations.ApiResponse(code = 200, message = "获取成功"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "报告未找到或未发布")
    })
    public ResponseEntity<MyApiResponse<ReportPublicResponse>> getPublicReport(
            @ApiParam(value = "报告的字符串ID", example = "report_ulid_mock_qwe")
            @PathVariable String reportStrId) {

        // --- Mock Logic ---
        // (在真实逻辑中: 检查 report.isPublished() 是否为 true)

        // --- Mock Data (假设已发布) ---
        ReportPublicResponse publicReport = new ReportPublicResponse();
        publicReport.setStrId(reportStrId);
        publicReport.setSourceName("模拟-工业排污口");
        publicReport.setSourceType("INDUSTRIAL");
        publicReport.setReportContent("## 模拟水质分析报告\n\n- **水源类型**: 工业\n- **近期浊度**: 平均 155.0 NTU\n- **分析**: 浊度偏高，建议启动过滤程序。\n");
        publicReport.setGeneratedAt(LocalDateTime.now().minusDays(1));

        MyApiResponse<ReportPublicResponse> response = new MyApiResponse<>("获取成功", publicReport);
        return ResponseEntity.ok(response);

        // --- Mock Data (假设未发布或未找到) ---
        // ApiResponse<ReportPublicResponse> notFoundResponse = new ApiResponse<>("报告未找到或未发布", null);
        // return new ResponseEntity<>(notFoundResponse, HttpStatus.NOT_FOUND);
    }
}
