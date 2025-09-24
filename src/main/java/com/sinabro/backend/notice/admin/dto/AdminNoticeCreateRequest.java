package com.sinabro.backend.notice.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminNoticeCreateRequest {
    private String title;       // 필수
    private String content;     // 필수
    private String noticeType;  // 선택
}
