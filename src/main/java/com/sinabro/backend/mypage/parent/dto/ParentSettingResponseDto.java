package com.sinabro.backend.mypage.parent.dto;

import lombok.*;

/**
 * [마이페이지 > 설정 프리필 응답]
 * - parent_setting(알림/이메일) + user.user_language 만 내려줌
 * - privacy_consent 는 화면에서 다루지 않으므로 제외
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParentSettingResponseDto {
    private boolean allowNotifications; // 알림 수신 동의
    private boolean emailSubscription;  // 이메일 수신 동의
    private String userLanguage;        // 언어 설정 (User.userLanguage)
}
