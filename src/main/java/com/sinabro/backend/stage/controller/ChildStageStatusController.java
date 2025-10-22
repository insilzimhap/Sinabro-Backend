package com.sinabro.backend.stage.controller;

import com.sinabro.backend.stage.dto.ChildStageStatusResponseDto;
import com.sinabro.backend.stage.service.ChildStageStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * [자녀별 학습 진행도 조회 API]
 *
 * - GET /api/app/child/{childId}/stage/all
 *     → 전체 Stage/Fruit 상태 조회 (모든 나무와 열매 상태)
 *
 * - GET /api/app/child/{childId}/stage/current
 *     → 현재 최고 활성 열매 + 스테이지(Level) + unlockedUntilByStage Map 반환
 *
 * - 듣기/쓰기/게임 공통으로 사용됨
 */
@RestController
@RequestMapping("/api/app/child")
@RequiredArgsConstructor
@Slf4j
public class ChildStageStatusController {

    private final ChildStageStatusService childStageStatusService;

    /** 🌳 [전체 진행 상태 조회]
     *  자녀별 전체 Stage/Fruit 활성 상태를 반환합니다.
     *  - 프론트에서 전체 트리 렌더링 또는 카테고리별 진행도 조회 시 사용
     *  예: /api/app/child/simtest/stage/all?category=writing_game
     */
    @GetMapping("/{childId}/stage/all") //changed
    public ResponseEntity<ChildStageStatusResponseDto> getChildStageStatusAll(
            @PathVariable String childId,
            @RequestParam(required = false) String category) { //changed
        log.info("[ChildStageStatusController] getChildStageStatusAll 호출 childId={} category={}", childId, category);
        var resp = childStageStatusService.getChildStageStatusAll(childId, category); //changed
        return ResponseEntity.ok(resp);
    }

    /** 🎯 [진행 요약 조회: 전체 + 카테고리 겸용]
     *  예:
     *   - /api/app/child/simtest/stage/ui/current                → 전체 요약
     *   - /api/app/child/simtest/stage/ui/current?category=writing_game → 특정 카테고리 요약
     */
    @GetMapping("/{childId}/stage/ui/current") //changed
    public ResponseEntity<ChildStageStatusResponseDto> getChildCurrentProgress(
            @PathVariable String childId,
            @RequestParam(required = false) String category) {

        if (category != null && !category.isBlank()) {
            log.info("[ChildStageStatusController] getChildCurrentProgressByCategory 호출 childId={} category={}", childId, category);
            var resp = childStageStatusService.getChildCurrentProgressByCategory(childId, category);
            return ResponseEntity.ok(resp);
        } else {
            log.info("[ChildStageStatusController] getChildCurrentProgress (전체) 호출 childId={}", childId);
            var resp = childStageStatusService.getChildCurrentProgress(childId);
            return ResponseEntity.ok(resp);
        }
    }


}