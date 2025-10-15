package com.sinabro.backend.stage.dto;

import lombok.*;
import java.util.List;

/**
 * [자녀별 전체 스테이지 진행도 DTO]
 * - Stage 단위로 자녀의 열매 활성 상태(ChildFruitStatusDto 리스트)를 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildStageStatusResponseDto {

    private String currentStageId;  // 현재 진행 중인 스테이지 ID
    private String currentFruitId;  // 가장 마지막으로 활성화된 열매 ID
    private String currentLevel;    // 현재 스테이지 레벨 (초급/중급/고급 등) //changed
    private String childId; // 자녀 ID
    private List<StageStatus> stages; // 전체 스테이지별 진행 정보

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StageStatus {
        private String stageId;   // 스테이지 ID
        private String category;  // listening_study, writing_game 등
        private String level;     // 초급, 중급, 고급
        private List<ChildFruitStatusDto> fruits; // 열매 상태 리스트
    }
}
