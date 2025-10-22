package com.sinabro.backend.stage.controller;

import com.sinabro.backend.stage.dto.StageWithFruitsDto;
import com.sinabro.backend.stage.service.StageQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 🌳 StageController
 * - 스테이지(나무) 관련 API 요청을 처리하는 컨트롤러
 * - 자녀별 전체 학습 맵(열매 활성 상태) 조회 기능을 제공함
 */
@RestController
@RequestMapping("/api/stages")
@RequiredArgsConstructor
public class StageController {

    private final StageQueryService stageQueryService;

    /**
     * 특정 자녀의 모든 스테이지 및 열매 활성 상태를 조회합니다.
     * @param childId 조회할 자녀의 ID
     * @return 200 OK와 함께 스테이지별로 그룹화된 열매 상태 DTO 리스트
     */
    @GetMapping("/status/{childId}")
    public ResponseEntity<List<StageWithFruitsDto>> getStageStatusForChild(@PathVariable String childId) {
        List<StageWithFruitsDto> response = stageQueryService.getStageStatusForChild(childId);
        return ResponseEntity.ok(response);
    }
}