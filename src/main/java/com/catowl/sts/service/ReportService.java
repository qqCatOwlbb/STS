package com.catowl.sts.service;

import com.catowl.sts.model.DTO.Request.ReportGenerateRequest;
import com.catowl.sts.model.DTO.Response.ReportResponse;

public interface ReportService {
    ReportResponse generateReport(ReportGenerateRequest request);
}
