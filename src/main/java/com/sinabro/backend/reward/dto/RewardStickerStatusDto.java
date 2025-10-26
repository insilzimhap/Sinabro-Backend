package com.sinabro.backend.reward.dto;

import lombok.*;

/**
 * 🎯 RewardStickerStatusDto
 * - 자녀별 현재 도감 내 스티커 현황 조회용 DTO
 * - ChildSticker + RewardSticker JOIN 결과
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardStickerStatusDto {
    private String stickerId;    // 스티커 ID (ST_LS_001)
    private String stickerName;  // 스티커 이름 (예: 남동생)
    private Boolean isObtained;  // 획득 여부
    private String dexId;        // 소속 도감 (DEX_LS_01)
    private int sequenceInDex;   // 도감 내 순서
}
