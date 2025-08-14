package com.sinabro.backend.admin.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class AdminNoticeListItemDto {
    private Long id;                 // notice_id
    private String title;            // 제목
    private LocalDateTime createdDate; // notice_created_date
    private String noticeType; //유형
}

