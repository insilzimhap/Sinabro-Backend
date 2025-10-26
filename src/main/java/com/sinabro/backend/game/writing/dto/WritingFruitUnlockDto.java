package com.sinabro.backend.game.writing.dto;

import lombok.*;


/**
 * [쓰기 게임 > 열매 해금 정보 DTO]
 * - 한 열매(세트) 완료 시 다음 열매 또는 다음 스테이지 첫 열매가 활성화되는 결과를 전달
 * - /complete API 응답에서 사용 (WritingGameCompleteResponseDto 내부 포함 가능)
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WritingFruitUnlockDto {
    private String currentFruitId;   // 현재 클리어한 열매 ID (Learning_Fruit.fruit_id)
    private boolean isSuccess;       // 이번 열매 통과 여부 (Writing_Game_Result.is_success)
    private String nextFruitId;      // 새로 열린 다음 열매 ID (없으면 null)
    private String nextStageId;      // 다음 Stage 첫 열매라면, 해당 Stage ID
}

