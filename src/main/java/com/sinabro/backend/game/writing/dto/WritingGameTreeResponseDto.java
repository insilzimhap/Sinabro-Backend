package com.sinabro.backend.game.writing.dto;

import lombok.*;

/**
 * [쓰기 게임 > 나무(열매 진행도) 응답 DTO]
 * - 특정 단계(stage)의 열매(fruit) 리스트를 반환
 * - 각 열매의 활성 상태와 마지막 플레이 결과(점수/성공 여부)를 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WritingGameTreeResponseDto {

    private String fruitId;       // 열매(세트) ID (Learning_Fruit.fruit_id)
    private String title;         // 열매 제목 (Learning_Fruit.title)
    private Boolean lastSuccess;  // 마지막 플레이 성공 여부 (Writing_Game_Result.is_success)
    private Integer lastScore;    // 마지막 플레이 점수 (Writing_Game_Result.wg_score)
    private boolean isActive;     // 활성 여부 (Learning_Fruit.is_active)
}
