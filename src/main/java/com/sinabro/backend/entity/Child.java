package com.sinabro.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "Child")
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

    @Column(name = "user_id", length = 255)
    private String userId; // FK → User.user_id
}
