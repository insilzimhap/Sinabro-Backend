package com.sinabro.backend.game.writing.dto;

import lombok.*;

/**
 * [쓰기 게임 > 완료 응답 DTO]
 * - 서버가 게임 완료 후 클라이언트에 반환하는 최소 정보
 * - 점수, 통과 여부, 소요 시간 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WritingGameCompleteResponseDto {

    private String resultId;       // 결과 ID
    private int score;             // 정답 수
    private boolean success;       // 통과 여부
    private Integer timeSpentSecs; // 소요 시간(초)
}
