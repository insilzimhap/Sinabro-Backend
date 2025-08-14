package com.sinabro.backend.admin.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class AdminNoticeDetailDto {
    private Long id;                   // notice_id
    private String title;              // 제목
    private String content;            // 내용
    private String noticeType;         // 유형 (이벤트/점검 등)
    private LocalDateTime createdDate; // 작성일
    private LocalDateTime updatedDate; // 수정일
}
