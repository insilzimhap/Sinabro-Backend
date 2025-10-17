package com.sinabro.backend.stage.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

/**
 * [자녀별 전체 스테이지 진행도 DTO]
 * - Stage 단위로 자녀의 열매 활성 상태(ChildFruitStatusDto 리스트)를 포함
 * - 진행도 API(단일 요약/전체 조회) 모두 공용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildStageStatusResponseDto {

    private String currentStageId;   // 현재 진행 중인 스테이지 ID
    private String currentFruitId;   // 가장 마지막으로 활성화된 열매 ID
    private String currentLevel;     // 현재 스테이지 레벨 (초급/중급/고급 등)
    private String childId;          // 자녀 ID
    private List<StageStatus> stages; // 전체 스테이지별 진행 정보

    // ✅ 하이브리드 진행도 응답용 필드 추가 (간단한 Map + 구조적 HighestFruit) //changed
    private Map<String, Integer> unlockedUntilByStage; // 각 Stage별 활성된 열매 개수 //changed
    private HighestFruit highest;                     // 최고 활성 열매 정보 //changed
    private String category;                          // listening_study, writing_game 등 //changed
    private int schemaVersion = 1;                    // 버전 (추후 확장 대비) //changed

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

    // ✅ 최고 활성 열매 정보 구조체 //changed
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HighestFruit {
        private String fruitId;         // 최고 활성 열매 ID
        private String stageId;         // 해당 열매의 스테이지 ID
        private int sequenceInStage;    // 스테이지 내 순서
    }
}
