package com.catowl.sts.service;

import com.catowl.sts.model.dto.Request.ReportGenerateRequest;
import com.catowl.sts.model.dto.Response.ReportResponse;
import com.catowl.sts.model.dto.Response.ReportStatusUpdateResponse;

import java.util.List;

public interface ReportService {

    ReportResponse generateReport(ReportGenerateRequest request);

    List<ReportResponse> getReportsForUser(Long userId);

    ReportResponse getReportByIdForUser(String reportStrId,Long userId);

    ReportStatusUpdateResponse publishReport(String reportStrId, Long userId);

    ReportStatusUpdateResponse unpublishReport(String reportStrId, Long userId);

    void deleteReport(String reportStrId,Long userId);
}
