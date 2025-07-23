package com.sinabro.backend.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "Parent_Settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentSettings {

    @Id
    @Column(name = "user_id", length = 255)
    private String userId; // FK → User.user_id

    @Column(name = "allow_notifications")
    private Boolean allowNotifications;

    @Column(name = "language_pref", length = 50)
    private String languagePref;

    @Column(name = "time_limit_mode")
    private Boolean timeLimitMode;

    @Column(name = "email_subscription")
    private Boolean emailSubscription;
}
