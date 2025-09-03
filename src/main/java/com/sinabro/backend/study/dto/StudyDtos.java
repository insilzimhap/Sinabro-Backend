package com.sinabro.backend.study.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 학습(듣기/쓰기)에서 쓰는 DTO를 한 파일로 모은 번들.
 * 컨트롤러/서비스에서는 StudyDtos.StageDTO 처럼 사용.
 */
public class StudyDtos {

    // ─────────────────────────────────────────────────────────────────────────────
    // 1) Stage(나무 단계) 조회용
    // ─────────────────────────────────────────────────────────────────────────────
    @Data
    @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = """
            스테이지(나무 단계) DTO
            - stageId: ST001 같은 고유 ID
            - category: 'listening_study' | 'writing_study'
            - level: '초급' | '중급' | '고급'
            """)
    public static class StageDTO {
        private String stageId;
        private String category;
        private String level;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 2) 듣기 학습 컨텐츠 조회용
    // ─────────────────────────────────────────────────────────────────────────────
    @Data
    @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = """
            듣기 학습 컨텐츠 DTO
            - contentId: 문제 ID
            - contentType: '듣기 학습 컨텐츠' (기본값)
            - subjectTag: 6종 제한 태그
            - imageUrl/audioUrl: 프론트는 항상 '$baseUrl{path}'로 로드
            - order: 단계 내 노출 순서
            """)
    public static class ListeningContentDTO {
        private String contentId;
        private String contentType;
        private String subjectTag;
        private String imageUrl;     // nullable
        private String audioUrl;     // nullable
        private Integer order;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 3) 쓰기 학습 컨텐츠 조회용
    // ─────────────────────────────────────────────────────────────────────────────
    @Data
    @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = """
            쓰기 학습 컨텐츠 DTO
            - contentId: 문제 ID
            - subjectTag: 12종 제한 태그
            - contentType: '쓰기 학습 컨텐츠'
            - contentText: 따라쓰기 텍스트(자음/단어/문장 등)
            - imageUrl/audioUrl/strokeImageUrl: 필요 리소스 경로(nullable)
            - order: 단계 내 노출 순서
            """)
    public static class WritingContentDTO {
        private String contentId;
        private String subjectTag;
        private String contentType;
        private String contentText;
        private String imageUrl;        // nullable
        private String audioUrl;        // nullable
        private String strokeImageUrl;  // nullable
        private Integer order;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 4) 듣기 학습 기록 저장 요청
    // ─────────────────────────────────────────────────────────────────────────────
    @Data
    @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = """
            듣기 학습 기록 저장 요청
            - childId: 자녀 PK
            - stageId: ST00x
            - contentId: 학습한 문제 ID
            - completed: 학습을 끝까지 진행했는지 여부
            - timeSpent: 소요시간 (예: '00:05:30')
            """)
    public static class ListeningRecordRequest {
        private String childId;
        private String stageId;
        private String contentId;
        private Boolean completed;
        private String timeSpent; // 'HH:mm:ss' 문자열로 받기 (서버에서 LocalTime 변환)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 5) 쓰기 학습 기록 저장 요청
    // ─────────────────────────────────────────────────────────────────────────────
    @Data
    @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = """
            쓰기 학습 기록 저장 요청
            - childId: 자녀 PK
            - stageId: ST01x
            - contentId: 학습한 문제 ID
            - completed: 학습을 끝까지 진행했는지 여부
            - timeSpentMinutes: 소요시간(분)
            """)
    public static class WritingRecordRequest {
        private String childId;
        private String stageId;
        private String contentId;
        private Boolean completed;
        private Integer timeSpentMinutes; // 분 단위
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 6) (선택) 잠금/오픈 맵 응답용
    // ─────────────────────────────────────────────────────────────────────────────
    @Data
    @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = """
            단계 잠금/완료 상태 DTO
            - stageId: 단계 ID
            - level: 초급/중급/고급
            - unlocked: 선택 가능 여부
            - completed: 완료 여부
            """)
    public static class StageStatusDTO {
        private String stageId;
        private String level;
        private Boolean unlocked;
        private Boolean completed;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 7) 공통 메시지 응답
    // ─────────────────────────────────────────────────────────────────────────────
    @Data
    @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "단순 메시지 응답")
    public static class SimpleMessage {
        private String message;
    }
}
