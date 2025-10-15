package com.sinabro.backend.game.listening.dto;

import lombok.*;
/*
 * 🎬 듣기 게임 시작 응답 DTO
 * 파일: com/sinabro/backend/game/listening/dto/ListeningGameStartResponseDto.java
 * 개요: 듣기 게임 시작 응답 DTO (resultId 반환)
 * - 게임 입장 시 세션 생성 결과와 열매 상태를 함께 반환
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListeningGameStartResponseDto {
    private String resultId;        // 생성된 세션 결과 ID (listening_game_result.lg_result_id)
    private Integer totalQuestions; // 열매 내 총 문항 수
    private boolean isActive;       // 자녀별 열매 활성 상태 (Child_Fruit_Status.is_active)
}
