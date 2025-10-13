package com.sinabro.backend.game.writing.dto;

import lombok.*;
import java.util.List;

/**
 * [쓰기 게임 > 시작 응답 DTO]
 * - 검증 통과 후 랜덤으로 출제된 문제 목록을 반환
 * - 프론트는 이 데이터를 기반으로 문제 화면 구성
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WritingGameStartResponseDto {

    private String resultId;          // 세션 결과 ID (랜덤 생성)
    private String fruitId;           // 열매(세트) ID
    private int questionCount;        // 출제 문항 수
    private List<WritingQuestionDto> questions; // 출제된 문제 리스트
}
