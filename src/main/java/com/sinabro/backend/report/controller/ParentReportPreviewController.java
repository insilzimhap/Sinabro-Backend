package com.sinabro.backend.report.controller;

// 다른 패키지의 클래스들을 import 해줘야 해
import com.sinabro.backend.report.dto.ReportRequestDto;
import com.sinabro.backend.report.dto.ReportResponseDto;
import com.sinabro.backend.report.service.ParentReportPreviewService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
public class ParentReportPreviewController {

    private final ParentReportPreviewService reportService;

    public ParentReportPreviewController(ParentReportPreviewService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/preview")
    public ResponseEntity<ReportResponseDto> createReportPreview(@RequestBody ReportRequestDto requestDto) {
        ReportResponseDto response = reportService.createReportPreview(requestDto);
        return ResponseEntity.ok(response);
    }
}