package com.sinabro.backend.reward.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "reward_dex")
@Getter
public class RewardDex {

    @Id
    @Column(name = "dex_id", length = 20, nullable = false)
    private String dexId;

    @Column(length = 50, nullable = false)
    private String category; // ENUM 타입이지만, 다른 패키지 의존성을 피하기 위해 String으로 선언

    @Column(name = "stage_id", length = 10, nullable = false)
    private String stageId;

    @Column(name = "dex_name", length = 100, nullable = false)
    private String dexName;

    @Column(name = "total_stickers", nullable = false)
    private int totalStickers;
}