package com.sinabro.backend.report.controller;

import com.sinabro.backend.report.dto.ReportRequest;
import com.sinabro.backend.report.service.ParentReportPreviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report")   // ✅ prefix: /api/report
@RequiredArgsConstructor
public class ParentReportPreviewController {

    private final ParentReportPreviewService previewService;

    // 부모가 자녀 리포트를 확인하는 API
    @PostMapping("/preview")
    public String preview(@RequestBody ReportRequest request) {
        // ✅ request.getChildId(), request.getDate() 정상 동작
        return previewService.generatePreview(request.getChildId(), request.getDate());
    }
}
