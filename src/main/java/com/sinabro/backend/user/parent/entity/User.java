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
    @Column(name = "user_id", length = 255)
    private String userId; // PK, 일반 ID or 소셜 UUID

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "user_pw", length = 255)
    private String userPw;

    @Column(name = "user_name", length = 255)
    private String userName;

    @Column(name = "user_phoneNum", length = 255)
    private String userPhoneNum;

    @Column(name = "user_language", length = 255)
    private String userLanguage = "Korea";

    @Column(name = "role", length = 20)
    private String role;

    @Column(name = "social_type", length = 50)
    private String socialType;

    @Column(name = "social_id", length = 255)
    private String socialId;

    @Column(name = "user_create_date", updatable = false)
    @CreationTimestamp
    private Timestamp userCreateDate;

    //부모-자녀 양방향 연동
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Child> children = new ArrayList<>();
}
