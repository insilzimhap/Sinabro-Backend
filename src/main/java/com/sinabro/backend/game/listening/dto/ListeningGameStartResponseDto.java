package com.sinabro.backend.game.listening.dto;

import lombok.*;
/*
 * 파일: com/sinabro/backend/game/listening/dto/ListeningGameStartResponseDto.java
 * 개요: 듣기 게임 시작 응답 DTO (resultId 반환)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListeningGameStartResponseDto {
    private String resultId;       // 생성된 세션 결과 ID (listening_game_result.lg_result_id)
    private Integer totalQuestions; // (옵션) 해당 열매 총 문항 수
}
