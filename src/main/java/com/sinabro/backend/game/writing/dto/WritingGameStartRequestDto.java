package com.sinabro.backend.game.writing.dto;

import lombok.*;

/**
 * [쓰기 게임 > 시작 요청 DTO]
 * - 자녀 ID와 열매 ID를 전달하여 게임 시작 가능 여부를 검증
 * - LearningFruit.is_active 및 category='writing_game' 확인용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WritingGameStartRequestDto {

    private String childId;   // 자녀 ID (Child.child_id)
    private String fruitId;   // 열매(세트) ID (Learning_Fruit.fruit_id)
}
