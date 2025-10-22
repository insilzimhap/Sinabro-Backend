package com.sinabro.backend.reward.entity;

import com.sinabro.backend.stage.entity.Stage;
import jakarta.persistence.*;
import lombok.*;



/**
 * 🎁 RewardDex (보상 도감)
 * - Stage 1개와 매핑되는 도감 정의 테이블
 * - ex: DEX_LS_01 (listening_study, ST001, 가족 도감)
 */

@Entity
@Table(name = "reward_dex")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardDex {

    // 도감 ID (PK)
    @Id
    @Column(name = "dex_id", length = 20, nullable = false)
    private String dexId;

    // 카테고리 ENUM (listening_study / writing_study)
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50, nullable = false)
    private Stage.Category category;

    // 연결된 Stage ID (FK)
    @Column(name = "stage_id", length = 10, nullable = false)
    private String stageId;

    // 도감 이름 (UI용)
    @Column(name = "dex_name", length = 100, nullable = false)
    private String dexName;

    // 도감 내 총 스티커 개수
    @Column(name = "total_stickers", nullable = false)
    private int totalStickers;

    // Stage 참조 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", insertable = false, updatable = false)
    private Stage stage;
}