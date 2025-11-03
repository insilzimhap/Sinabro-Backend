package com.sinabro.backend.mypage.parent.dto;

import lombok.*;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 마이페이지(프로필 화면)에 보여줄 부모 정보
public class ParentProfileResponseDto {
    private String userId;   //User.userId (PK)
    private String userName;   //User.userName
    private String userEmail;  //User.userEmail
    private String userPhoneNum;  //User.userPhoneNum (nullable)

}
