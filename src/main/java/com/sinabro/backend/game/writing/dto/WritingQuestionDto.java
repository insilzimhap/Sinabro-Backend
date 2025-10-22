package com.sinabro.backend.game.writing.dto;

import lombok.*;

/**
 * [쓰기 게임 > 문제 DTO]
 * - 프론트로 전달되는 단일 문제 단위 데이터
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WritingQuestionDto {

    private String wgQuestionId;     // 문제 ID
    private String fruitId;          // 열매 ID
    private String wgSubjectTag;     // 문제 태그 (예: 자음, 곡선1, 동물 등)
    private String wgCorrectAnswer;  // 정답 텍스트 (프론트 라벨링용)
}
