package com.sinabro.backend.inquiry.app.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * [문의 상세 DTO]
 * - 본문 포함
 * - 최신 답변 1건만 포함(없으면 null)
 */
@Getter
@AllArgsConstructor
public class InquiryDetailDto {
    private Long id;                 // inquiry_id
    private String title;            // inquiry_title
    private String authorName;       // parent.user_name

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime createdAt; // inquiry_created_at

    private String status;           // "답변 전" | "답변 완료"
    private String content;          // inquiry_content

    private ReplyDto reply;          // 최신 답변(없으면 null)

    @Getter
    @AllArgsConstructor
    public static class ReplyDto {
        private Long id;             // reply_id
        private String adminName;    // 관리자 표시 이름(예: "팀 시나브로")
        private String content;      // reply_content

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime createdDate; // reply_created_date
    }
}
