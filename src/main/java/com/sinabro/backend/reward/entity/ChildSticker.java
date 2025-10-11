package com.sinabro.backend.reward.entity;

import com.sinabro.backend.user.entity.Child;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.sql.Timestamp;

@Entity
@Table(name = "child_sticker")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@IdClass(ChildStickerId.class) // 복합 키 클래스 지정
public class ChildSticker {

    @Id
    @Column(name = "child_id")
    private String childId;

    @Id
    // ✅ 여기에 DB 스키마와 동일한 length = 20을 추가해줘!
    @Column(name = "sticker_id", length = 20)
    private String stickerId;

    @Column(nullable = false)
    private boolean isObtained = false;

    private Timestamp obtainedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Child child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sticker_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private RewardSticker rewardSticker;
}