package com.sinabro.backend.progress.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder // 생성 편의를 위해 Builder 추가
public class ProgressSummaryDto {
    private Double progressToNextLevel;     // 다음 레벨 진행률 (0.0 ~ 1.0)
    // ⭐️ 모든 카테고리 필드 추가
    private String listeningStudyRecent;
    private String listeningStudyBest;
    private String writingStudyRecent;
    private String writingStudyBest;
    private String listeningGameRecent;
    private String listeningGameBest;
    private String writingGameRecent;
    private String writingGameBest;
}