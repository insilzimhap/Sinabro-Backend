package com.sinabro.backend.admin.inquiry.dto;


import lombok.*;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class AdminInquiryDetailDto {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private String writerId;              // 작성자 ID (검색/식별용)
    private String writerName;            // 작성자 이름
    private String status;                // 답변 상태 ('답변 전' / '답변 완료')

    // 관리자 답변(가장 최근 1건만 사용)
    private Long replyId;          // 답변 ID (없으면 null)
    private String replyContent;   // 답변 내용 (없으면 null/빈문자열)
    private LocalDateTime replyCreatedDate; // 답변 작성일 (없으면 null)
}
