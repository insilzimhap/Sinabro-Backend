package com.sinabro.backend.user.parent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserRegisterRequest {
    @NotBlank private String userId;
    @NotBlank private String userEmail;
    @NotBlank private String userPw;
    @NotBlank private String confirmPw;

    private String userName;
    private String userPhoneNum;
    private String userLanguage; // 기본값은 서비스/엔티티에서 Korea
    private String role;         // parent
    private String socialType;   // local
}
