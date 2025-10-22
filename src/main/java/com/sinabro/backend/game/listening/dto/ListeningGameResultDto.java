package com.sinabro.backend.game.listening.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [듣기 게임 > 결과 저장 DTO]
 * - 게임 완료 시 결과(Result) 테이블에 저장되는 정보
 * - Child / Fruit / Score / Success / 소요시간 포함
 */

@Getter
@NoArgsConstructor
public class ListeningGameResultDto {
    private String childId;        // 자녀 ID (Child.child_id)
    private String fruitId;        // 열매(세트) ID (Learning_Fruit.fruit_id)
    private int score;             // 정답 개수 (Listening_Game_Result.lg_score)
    private int totalQuestions;    // 전체 문항 수 (Listening_Game_Result.total_questions)
    private boolean isSuccess;     // 통과 여부 (Listening_Game_Result.is_success)
    private int timeSpentSecs;     // 소요 시간(초) (Listening_Game_Result.time_spent_secs)
}