package com.sinabro.backend.game.writing.dto;

import lombok.*;

/**
 * [쓰기 게임 > 완료 요청 DTO]
 * - 한 열매(세트) 내 모든 문제를 완료한 뒤 결과 저장용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WritingGameCompleteRequestDto {

    private String childId;        // 자녀 ID (Child.child_id)
    private String fruitId;        // 열매 ID (Learning_Fruit.fruit_id)
    private String resultId;       // 세션 결과 ID (Writing_Game_Result.wg_result_id)
    private Integer timeSpentSecs; // 소요 시간(초)
}
