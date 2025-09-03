package com.sinabro.backend.stage.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stage")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Stage {

    // stage_id PK (예: ST001)
    @Id
    @Column(name = "stage_id", length = 10, nullable = false)
    private String stageId;

    // - ENUM: 'writing_study','listening_study','writing_game','listening_game'
    // - 문자열로 두고 DB 레벨에서 ENUM 제약
    @Column(
            name = "category",
            nullable = false,
            columnDefinition = "ENUM('writing_study','listening_study','writing_game','listening_game')"
    )
    private String category;

    // 초급 / 중급 / 고급 (자유 문자열)
    @Column(name = "level", length = 10, nullable = false)
    private String level;
}
