package com.sinabro.backend.reward.dto;

import lombok.*;

/**
 * 🎁 RewardStickerRequestDto
 * - 보상(스티커) 지급 요청용 DTO
 * - 학습 완료 시 호출
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardStickerRequestDto {
    private String childId;   // 자녀 ID
    private String fruitId;   // 학습 완료한 열매 ID (ex: FR_LS_008)
}
