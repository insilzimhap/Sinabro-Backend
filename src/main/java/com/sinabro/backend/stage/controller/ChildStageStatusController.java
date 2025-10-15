package com.sinabro.backend.stage.controller;

import com.sinabro.backend.stage.dto.ChildStageStatusResponseDto;
import com.sinabro.backend.stage.service.ChildStageStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * [자녀별 학습 진행도 조회 API]
 * - GET /api/app/child/{childId}/stage/all        → 전체 Stage/Fruit 상태 조회
 * - GET /api/app/child/{childId}/stage/current    → 현재 최고 활성 열매 + 스테이지(Level)
 * - 듣기/쓰기/게임 공통으로 사용됨
 */
@RestController
@RequestMapping("/api/app/child")
@RequiredArgsConstructor
@Slf4j
public class ChildStageStatusController {

    private final ChildStageStatusService childStageStatusService;

    /** 🌳 자녀별 전체 Stage/Fruit 활성 상태 조회 */
    @GetMapping("/{childId}/stage/all") //changed
    public ResponseEntity<ChildStageStatusResponseDto> getChildStageStatusAll(@PathVariable String childId) { //changed
        log.info("[ChildStageStatusController] getChildStageStatusAll 호출 childId={}", childId); //changed
        var resp = childStageStatusService.getChildStageStatusAll(childId); //changed
        return ResponseEntity.ok(resp);
    }

    /** 🏆 자녀별 현재 최고 활성 열매 + 스테이지(Level) 조회 */ //changed
    @GetMapping("/{childId}/stage/current") //changed
    public ResponseEntity<ChildStageStatusResponseDto> getChildCurrentProgress(@PathVariable String childId) { //changed
        log.info("[ChildStageStatusController] getChildCurrentProgress 호출 childId={}", childId); //changed
        var resp = childStageStatusService.getChildCurrentProgress(childId); //changed
        return ResponseEntity.ok(resp); //changed
    }
}
