package com.sinabro.backend.user.child.entity;

import com.sinabro.backend.user.parent.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "child")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Child {

    @Id
    @Column(name = "child_id", length = 255, nullable = false)
    private String childId;

    @Column(name = "child_name", length = 255, nullable = false)
    private String childName;

    // 컬럼명 ERD와 동일: child_nickname
    @Column(name = "child_nickname", length = 255, nullable = false)
    private String childNickname;

    @Column(name = "child_birth", length = 255, nullable = false)
    private String childBirth;

    @Column(name = "child_age", nullable = false)
    private Integer childAge;

    @Column(name = "child_pw", length = 255, nullable = false)
    private String childPw;

    // **자녀 학습 레벨**: NULL 허용, 레벨 테스트 후 1/2/3 저장
    @Column(name = "child_level")
    private Integer childLevel;

    @Column(name = "time_limit_minutes", nullable = false)
    private Integer timeLimitMinutes;

    @Column(name = "role", length = 20, nullable = false)
    private String role;

    @Column(name = "child_create_date", updatable = false, nullable = false)
    @CreationTimestamp
    private Timestamp childCreateDate;

    @Column(name = "child_update_date")
    @UpdateTimestamp
    private Timestamp childUpdateDate;

    //부모와의 관계 = FK (NOT NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // FK -> User.user_id
    private User parent;

    // 기본값 보장
    @PrePersist
    public void prePersist() {
        if (this.timeLimitMinutes == null) this.timeLimitMinutes = 0;
        if (this.role == null) this.role = "child";
    }
}
