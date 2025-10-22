package com.sinabro.backend.reward.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * 🎁 RewardStickerResponseDto
 * - 보상(스티커) 지급 결과 응답용 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardStickerResponseDto {
    private Boolean success;          // true: 새로 획득, false: 이미 보유
    private String message;           // 메시지 ("스티커 획득 완료", "이미 획득한 스티커")
    private String stickerId;         // 획득한 스티커 ID
    private String stickerName;       // 스티커 이름 (ex: 사람_산책)
    private LocalDateTime obtainedAt; // 획득 시각
}
