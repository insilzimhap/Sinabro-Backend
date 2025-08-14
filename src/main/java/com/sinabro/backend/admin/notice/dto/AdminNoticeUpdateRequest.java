package com.sinabro.backend.admin.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class AdminNoticeUpdateRequest {
    private String title;       // 변경할 제목
    private String content;     // 변경할 내용
    private String noticeType;  // 변경할 유형
}
