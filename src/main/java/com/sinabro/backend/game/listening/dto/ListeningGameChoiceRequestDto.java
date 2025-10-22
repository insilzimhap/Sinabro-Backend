package com.sinabro.backend.game.listening.dto;

import lombok.*;

/**
 * [듣기 게임 > 문제 선택 요청 DTO]
 * - 한 문제에 대해 사용자가 어떤 보기를 선택했는지 서버에 전달
 * - UNIQUE(lg_result_id, lg_question_id) 제약으로 중복 선택 방지
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListeningGameChoiceRequestDto {

    private String resultId;    // 세션 결과 ID (ListeningGameResult.lg_result_id)
    private String questionId;  // 문제 ID (Listening_Game_Question.lg_question_id)
    private String optionId;    // 선택한 보기 ID (Listening_Game_Option.lg_option_id)
}
