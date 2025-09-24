package com.sinabro.backend.stage.entity;

import jakarta.persistence.*;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "stage")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "학습 단계(나무) 엔티티")
public class Stage {

    @Id
    @Column(name = "stage_id", length = 10, nullable = false)
    @Schema(description = "스테이지 ID (예: ST001)", example = "ST001")
    private String stageId;

    @Column(
            name = "category",
            nullable = false,
            columnDefinition = "ENUM('writing_study','listening_study','writing_game','listening_game')"
    )
    @Schema(description = "카테고리", example = "listening_study")
    private String category;

    @Column(name = "level", length = 10, nullable = false)
    @Schema(description = "레벨 (초급, 중급, 고급)", example = "초급")
    private String level;
}
