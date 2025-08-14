package com.sinabro.backend.admin.inquiry.dto;


import lombok.*;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class AdminInquiryListItemDto {
    private Long id;               // 문의 ID
    private String title;          // 제목
    private LocalDateTime createdAt; // 작성일
    private String writerId;          // 작성자 ID (검색/식별용)
    private String writerName;     // 작성자 이름 (parent.user_name)
    private String status;         // '답변 전' | '답변 완료'
}
