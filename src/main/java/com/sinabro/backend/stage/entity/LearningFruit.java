package com.sinabro.backend.stage.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "learning_fruit")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "열매(세트) 엔티티 - Stage 안에 묶이는 최소 학습 단위")
public class LearningFruit {

    @Id
    @Column(name = "fruit_id", length = 20, nullable = false)
    @Schema(description = "열매 ID", example = "FR_LS_001")
    private String fruitId;

    @Column(
            name = "category",
            nullable = false,
            columnDefinition = "ENUM('writing_study','listening_study','writing_game','listening_game')"
    )
    @Schema(description = "열매 카테고리", example = "listening_study")
    private String category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stage_id", referencedColumnName = "stage_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Schema(description = "소속 Stage ID")
    private Stage stage;

    @Column(name = "sequence_in_stage", nullable = false)
    @Schema(description = "해당 나무 내 열매 순서", example = "1")
    private Integer sequenceInStage;

    @Column(name = "title", length = 100)
    @Schema(description = "UI 표시용 제목", example = "기본 색상 A")
    private String title;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Schema(description = "활성 여부", example = "true")
    private Boolean isActive;
}
