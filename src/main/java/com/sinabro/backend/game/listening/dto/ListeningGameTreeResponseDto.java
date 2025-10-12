package com.sinabro.backend.game.listening.dto;

import lombok.*;

/**
 * [듣기 게임 > 나무(열매) 조회 응답 DTO]
 * - 특정 단계(stage)의 모든 열매 상태를 내려줌
 * - 각 열매의 활성 여부, 최근 성공 여부, 최근 점수를 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListeningGameTreeResponseDto {

    private String fruitId;      // 열매 ID (Learning_Fruit.fruit_id)
    private String title;        // 열매 이름 (Learning_Fruit.title)
    private boolean isActive;    // 활성화 여부 (Learning_Fruit.is_active)
    private Boolean lastSuccess; // 마지막 성공 여부 (null 가능) (Listening_Game_Result.is_success)
    private Integer lastScore;   // 마지막 점수 (null 가능) (Listening_Game_Result.lg_score)
}