package com.sinabro.backend.entity.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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
    @Column(name = "child_id", length = 255)
    private String childId;

    @Column(name = "child_name", length = 255)
    private String childName;

    @Column(name = "child_nickName", length = 255)
    private String childNickName;

    @Column(name = "child_birth", length = 255)
    private String childBirth;

    @Column(name = "child_age")
    private Integer childAge;

    @Column(name = "child_pw", length = 255)
    private String childPw;

    @Column(name = "child_level", length = 10)
    private String childLevel;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes = 0;

    @Column(name = "role", length = 20)
    private String role = "child";

    @Column(name = "child_create_date", updatable = false)
    @CreationTimestamp
    private Timestamp childCreateDate;

    //부모와의 관계 = FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // FK -> User.user_id
    private User parent;
}
