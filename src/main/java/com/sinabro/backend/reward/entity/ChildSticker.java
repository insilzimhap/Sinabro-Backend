package com.sinabro.backend.reward.entity;

import com.sinabro.backend.user.entity.Child;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.sql.Timestamp;

/**
 * 👦 ChildSticker (자녀별 스티커 현황)
 * - 자녀 + 스티커 복합키
 * - 획득 여부 / 시각 관리
 */
@Entity
@Table(name = "child_sticker")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ChildStickerId.class)
public class ChildSticker {

    // 복합키 1: 자녀 ID
    @Id
    @Column(name = "child_id", length = 255, nullable = false)
    private String childId;

    // 복합키 2: 스티커 ID
    @Id
    @Column(name = "sticker_id", length = 20, nullable = false)
    private String stickerId;

    // 획득 여부 (tinyint(1))
    @Column(name = "is_obtained", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isObtained = false;

    // 획득 시각 (null 가능)
    @Column(name = "obtained_at")
    private Timestamp obtainedAt;

    // FK: 자녀 (Child)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Child child;

    // FK: 스티커 (RewardSticker)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sticker_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private RewardSticker rewardSticker;
}