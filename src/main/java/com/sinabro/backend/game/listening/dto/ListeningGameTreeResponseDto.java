package com.sinabro.backend.game.listening.dto;

import lombok.*;

/**
 * [듣기 게임 > 나무(열매) 조회 응답 DTO]
 * - 특정 단계(stage)의 모든 열매 상태를 내려줌
 * - 자녀별 활성 여부는 Child_Fruit_Status 기준
 * - 최신 성공 여부 및 점수는 Listening_Game_Result 기반
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListeningGameTreeResponseDto {

    private String fruitId;       // 열매 ID (Learning_Fruit.fruit_id)
    private String title;         // 열매 제목 (Learning_Fruit.title)
    private boolean isActive;     // 활성화 여부 (Child_Fruit_Status.is_active 기준)
    private Boolean lastSuccess;  // 마지막 성공 여부 (Listening_Game_Result.is_success)
    private Integer lastScore;    // 마지막 점수 (Listening_Game_Result.lg_score)
}