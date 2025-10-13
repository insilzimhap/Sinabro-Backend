package com.sinabro.backend.game.listening.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 🎧 듣기 게임 완료 응답 DTO
 * - 서버가 게임 완료 후 클라이언트에 반환하는 최소 정보
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListeningGameCompleteResponseDto {
    private String resultId;    // 세션 결과 ID
    private int score;          // 정답 개수
    private boolean success;    // 통과 여부
    private Integer timeSpentSecs; // (옵션) 소요 시간
}
