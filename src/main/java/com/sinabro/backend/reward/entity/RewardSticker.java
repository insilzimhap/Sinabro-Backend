package com.sinabro.backend.reward.entity;

import com.sinabro.backend.stage.entity.LearningFruit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


/**
 * 🍬 RewardSticker (도감 내 스티커 정의)
 * - 도감(RewardDex)과 열매(LearningFruit)를 연결
 * - 열매 1개 완료 시 해당 스티커 해금
 */
@Entity
@Table(
        name = "reward_sticker",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_dex_seq", columnNames = {"dex_id", "sequence_in_dex"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardSticker {

    // 스티커 ID (PK)
    @Id
    @Column(name = "sticker_id", length = 20, nullable = false)
    private String stickerId;

    // 도감 ID (FK → RewardDex.dex_id)
    @Column(name = "dex_id", length = 20, nullable = false)
    private String dexId;

    // 소속 열매 (FK → LearningFruit.fruit_id)
    @Column(name = "fruit_id", length = 20, nullable = false)
    private String fruitId;

    // 스티커 이름 (UI용)
    @Column(name = "sticker_name", length = 100)
    private String stickerName;

    // 도감 내 순서
    @Column(name = "sequence_in_dex", nullable = false)
    private int sequenceInDex;

    // ✅ RewardDex FK 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dex_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private RewardDex rewardDex;

    // ✅ LearningFruit FK 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fruit_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private LearningFruit learningFruit;
}