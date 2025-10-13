package com.sinabro.backend.game.writing.dto;

import lombok.*;

/**
 * [쓰기 게임 > 선택 기록 요청 DTO]
 * - 자녀가 필기한 결과를 서버에 기록하는 요청 데이터
 * - 프론트에서 필기 인식 및 채점 결과를 포함해 전달
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WritingGameChoiceRequestDto {

    private String resultId;           // 세션 결과 ID (Writing_Game_Result.wg_result_id)
    private String questionId;         // 문제 ID (Writing_Game_Question.wg_question_id)
    private String childWrittenText;   // 자녀가 쓴 텍스트 (필기 인식 결과 1순위)
    private boolean isCorrect;         // 채점 결과 (프론트 측 판별)
}
