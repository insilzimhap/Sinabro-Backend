package com.sinabro.backend.user.entity;

import com.sinabro.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parent_setting")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ParentSetting {

    /** PK = FK (user.user_id) */
    @Id
    @Column(name = "user_id", length = 255, nullable = false)
    private String userId;

    /** 공유 PK 매핑 */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private User parent;

    @Column(name = "allow_notifications", nullable = false)
    private boolean allowNotifications;

    @Column(name = "email_subscription", nullable = false)
    private boolean emailSubscription;

    @Column(name = "privacy_consent", nullable = false)
    private boolean privacyConsent;
}
