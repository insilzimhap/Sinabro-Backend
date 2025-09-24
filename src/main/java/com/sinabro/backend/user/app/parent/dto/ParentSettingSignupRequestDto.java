package com.sinabro.backend.user.app.parent.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.*;

/**
 * [회원가입 시 parent_setting 입력 DTO]
 * - allow/email 은 선택(미전달 시 false로 간주)
 * - privacyConsent 은 반드시 true 여야 가입 가능
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ParentSettingSignupRequestDto {

    /** 알림 수신 동의 (선택) - 기본 false */
    private Boolean allowNotifications;

    /** 이메일 수신 동의 (선택) - 기본 false */
    private Boolean emailSubscription;

    /** 개인정보 수집/이용 동의 (필수) - true 여야 가입 가능 */
    @AssertTrue(message = "개인정보 수집·이용에 동의해야 가입할 수 있습니다.")
    private Boolean privacyConsent;
}
