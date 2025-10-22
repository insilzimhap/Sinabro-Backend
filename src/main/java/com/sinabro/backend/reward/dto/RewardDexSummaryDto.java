package com.sinabro.backend.reward.dto;

import lombok.*;

/**
 * 🧩 RewardDexSummaryDto
 * - 각 도감별 스티커 진행 현황 요약 DTO
 * - UI ProgressBar 등에 사용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardDexSummaryDto {
    private String dexId;        // 도감 ID (DEX_LS_01)
    private String dexName;      // 도감 이름 (가족 도감)
    private String category;     // 카테고리 (listening_study / writing_study)
    private int totalStickers;   // 도감 내 총 스티커 수
    private int obtainedCount;   // 자녀가 획득한 개수
}
