package com.sinabro.backend.game.listening.dto;

import lombok.*;

/**
 * 🎧 듣기 게임 완료 응답 DTO
 * - 게임 완료 후 결과(점수, 성공 여부 등)만 클라이언트로 반환
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListeningGameCompleteRequestDto {

    private String childId;       // 자녀 ID (Child.child_id)
    private String fruitId;       // 열매(세트) ID (Learning_Fruit.fruit_id)
    private String resultId;      // 세션 결과 ID (Listening_Game_Result.lg_result_id)
    private Integer timeSpentSecs; // 소요 시간(초) (Listening_Game_Result.time_spent_secs)
}
