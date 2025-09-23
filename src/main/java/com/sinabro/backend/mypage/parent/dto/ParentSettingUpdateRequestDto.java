package com.sinabro.backend.mypage.parent.dto;

import lombok.*;

/**
 * [마이페이지 > 설정 업데이트 요청]
 * - null 필드는 변경하지 않음(부분 업데이트)
 * - privacy_consent 는 화면/정책상 제외
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParentSettingUpdateRequestDto {
    private Boolean allowNotifications; // 변경 시에만 값 전달
    private Boolean emailSubscription;  // 변경 시에만 값 전달
    private String userLanguage;        // 변경 시에만 값 전달 ("Korea" 등)
}

