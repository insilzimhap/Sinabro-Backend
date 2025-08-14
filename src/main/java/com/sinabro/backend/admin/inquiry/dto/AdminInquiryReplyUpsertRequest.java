package com.sinabro.backend.admin.inquiry.dto;

import lombok.*;

@Getter
@NoArgsConstructor
public class AdminInquiryReplyUpsertRequest {
    private String content;      // 필수: 답변 내용
    private String adminUserId;  // 선택: 답변자 ID(=users.user_id). 미전달 시 'admin' 같은 기본값 사용 가능
}
