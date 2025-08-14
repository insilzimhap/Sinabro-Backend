package com.sinabro.backend.admin.user.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user")
public class AdminUser {
    @Id
    @Column(name = "user_id")
    private String id;

    @Column(name = "user_email", nullable = false)
    private String email;

    @Column(name = "user_name", nullable = false)
    private String name;

    @Column(name = "user_pw", nullable = false)
    private String password;

    @Column(name = "user_phoneNum", nullable = false)
    private String phoneNumber;

    @Column(name = "user_language", nullable = false)
    private String language;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "user_create_date", nullable = false)
    private LocalDateTime createDate;

//    @Column(name = "user_update_date")
//    private LocalDateTime updateDate;


}
