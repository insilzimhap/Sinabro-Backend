package com.sinabro.backend.progress.controller;

import com.sinabro.backend.progress.dto.ProgressSummaryDto;
import com.sinabro.backend.progress.service.ProgressQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/app/child") // 기본 경로
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressQueryService progressQueryService;

    /**
     * GET /api/app/child/{childId}/progress-summary
     * 자녀의 학습/게임 진행 상황 요약 정보 조회
     */
    @GetMapping("/{childId}/progress-summary")
    public ResponseEntity<ProgressSummaryDto> getProgressSummary(@PathVariable String childId) {
        log.info("[ProgressController] 진행 상황 요약 조회 요청: childId={}", childId);
        ProgressSummaryDto summary = progressQueryService.getProgressSummary(childId);
        log.info("[ProgressController] 진행 상황 요약 조회 응답: childId={}", childId);
        return ResponseEntity.ok(summary);
    }

}