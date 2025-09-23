package com.sinabro.backend.inquiry.app.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * [문의 리스트 아이템 DTO]
 * - 리스트 첫 화면용 (가볍게)
 * - 본문 미포함
 */
@Getter
@AllArgsConstructor
public class InquiryListItemDto {
    private Long id;                 // inquiry_id
    private String title;            // inquiry_title
    private String authorName;       // parent.user_name

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime createdAt; // inquiry_created_at

    private String status;           // "답변 전" | "답변 완료" (inquiry_status)
}
