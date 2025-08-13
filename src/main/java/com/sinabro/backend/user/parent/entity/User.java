package com.sinabro.backend.user.parent.entity;

import com.sinabro.backend.user.child.entity.Child;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "User")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "user_id", length = 255, nullable = false)
    private String userId; // PK, 일반 ID or 소셜 UUID

    @Column(name = "user_email", length = 255, nullable = false)
    private String userEmail;

    // 소셜 로그인은 비밀번호가 없을 수 있음
    @Column(name = "user_pw", length = 255)
    private String userPw;

    @Column(name = "user_name", length = 255, nullable = false)
    private String userName;

    // ✅ NULL 허용으로 변경 (소셜 가입 시 번호 없을 수 있음)
    @Column(name = "user_phone_num", length = 255, nullable = true)
    private String userPhoneNum;

    @Column(name = "user_language", length = 255, nullable = false)
    private String userLanguage;

    @Column(name = "role", length = 20, nullable = false)
    private String role;

    @Column(name = "social_type", length = 50)
    private String socialType;

    @Column(name = "social_id", length = 255)
    private String socialId;

    @Column(name = "user_create_date", updatable = false, nullable = false)
    @CreationTimestamp
    private Timestamp userCreateDate;

    //부모-자녀 양방향 연동
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Child> children = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.userLanguage == null || this.userLanguage.isBlank()) {
            this.userLanguage = "Korea";
        }
        if (this.role == null || this.role.isBlank()) {
            this.role = "parent";
        }
    }
}
