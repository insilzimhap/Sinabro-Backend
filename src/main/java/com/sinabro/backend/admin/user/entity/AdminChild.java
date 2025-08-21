package com.sinabro.backend.admin.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;

@Entity
@Table(name = "child")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminChild {
    // PK
    @Id
    @Column(name = "child_id", length = 255, nullable = false)
    private String id;                                   // child_id

    // 부모 FK -> user.user_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private AdminUser parent;                            // 부모

    // 기본 프로필
    @Column(name = "child_name", length = 255, nullable = false)
    private String name;                                 // child_name

    @Column(name = "child_nickname", length = 255, nullable = false)
    private String nickname;                             // child_nickname

    @Column(name = "child_birth", length = 255, nullable = false)
    private String birth;                                // child_birth (문자열 보관 정책)

    @Column(name = "child_age", nullable = false)
    private Integer age;                                 // child_age

    // 인증/레벨
    @Column(name = "child_pw", length = 255, nullable = false)
    private String password;                             // child_pw (NOT NULL)

    @Column(name = "child_level")
    private Integer level;                               // child_level (NULL 허용)

    // 이용 제한/역할
    @Column(name = "time_limit_minutes", nullable = false)
    private Integer timeLimitMinutes;                    // time_limit_minutes

    @Column(name = "role", length = 20, nullable = false)
    private String role;                                 // role

    // 타임스탬프 (DDL: datetime(6))
    @Column(name = "child_create_date", nullable = false, updatable = false)
    private Timestamp createDate;                        // child_create_date

    @Column(name = "child_update_date")
    private Timestamp updateDate;                        // child_update_date

    // 기본값 세팅 (DB/코드 양쪽에서 안전망)
    @PrePersist
    public void prePersist() {
        if (this.timeLimitMinutes == null) this.timeLimitMinutes = 0;
        if (this.role == null || this.role.isBlank()) this.role = "child";
    }
}
