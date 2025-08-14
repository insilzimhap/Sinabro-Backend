package com.sinabro.backend.admin.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "child")
@Getter
@Setter
@NoArgsConstructor
public class AdminChild {
    @Id
    @Column(name = "child_id")
    private String id;                    // 자녀 고유 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private AdminUser parent;             // 부모(FK) - AdminUser와 연관관계

    @Column(name = "child_name", nullable = false)
    private String name;                  // 자녀 이름

    @Column(name = "child_age", nullable = false)
    private Integer age;                  // 자녀 나이

    @Column(name = "child_pw")
    private String password;              // 자녀 비밀번호 (NULL 허용 가능)

    @Column(name = "child_level")
    private Integer level;                // 자녀 학습 레벨 (NULL 허용)

    // 필요 시 조회용으로 두면 좋아요
    @Column(name = "child_create_date", nullable = false)
    private LocalDateTime createDate;

//    @Column(name = "child_update_date")
//    private LocalDateTime updateDate;
}
