package com.sinabro.backend.reward.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "reward_sticker")
@Getter
public class RewardSticker {

    @Id
    @Column(name = "sticker_id", length = 20, nullable = false)
    private String stickerId;

    @Column(name = "dex_id", length = 20, nullable = false)
    private String dexId;

    @Column(name = "fruit_id", length = 20, nullable = false, unique = true)
    private String fruitId;

    @Column(name = "sticker_name", length = 100)
    private String stickerName;

    @Column(name = "sequence_in_dex", nullable = false)
    private int sequenceInDex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dex_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private RewardDex rewardDex;
}