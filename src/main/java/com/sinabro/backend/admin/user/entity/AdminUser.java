package com.sinabro.backend.admin.user.entity;

import com.sinabro.backend.admin.user.entity.AdminChild;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "User") // ✅ 실제 테이블명 대소문자 일치
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUser {
    @Id
    @Column(name = "user_id", length = 255, nullable = false)
    private String id; // PK, 일반 ID or 소셜 UUID

    @Column(name = "user_email", length = 255, nullable = false)
    private String email;

    // 소셜 로그인은 비밀번호가 없을 수 있음
    @Column(name = "user_pw", length = 255, nullable = true) // ✅ NULL 허용
    private String password;

    @Column(name = "user_name", length = 255, nullable = false)
    private String name;

    // ✅ NULL 허용으로 변경 (소셜 가입 시 번호 없을 수 있음)
    @Column(name = "user_phone_num", length = 255, nullable = true) // ✅ 컬럼명 정확히 일치
    private String phoneNumber;

    @Column(name = "user_language", length = 255, nullable = false)
    private String language;

    @Column(name = "role", length = 20, nullable = false)
    private String role;

    @Column(name = "social_type", length = 50)
    private String socialType;

    @Column(name = "social_id", length = 255)
    private String socialId;

    @Column(name = "user_create_date", updatable = false, nullable = false)
    @CreationTimestamp // ✅ DB DEFAULT + JPA persist 모두 커버
    private Timestamp createDate;

    //부모-자녀 양방향 연동 (Admin 도메인에서 자녀 조회/관리하려면 유지)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<AdminChild> children = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        // 기본값 보장 (모바일 엔티티와 동일 정책)
        if (this.language == null || this.language.isBlank()) this.language = "Korea";
        if (this.role == null || this.role.isBlank()) this.role = "parent";
    }


}
