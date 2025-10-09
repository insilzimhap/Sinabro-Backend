package com.sinabro.backend.stage.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "learning_fruit",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_stage_seq", columnNames = {"stage_id", "sequence_in_stage"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningFruit {

    @Id
    @Column(name = "fruit_id", length = 20, nullable = false)
    private String fruitId;

    // ENUM: writing_study, listening_study, writing_game, listening_game
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_fruit_stage"))
    private Stage stage;

    @Column(name = "sequence_in_stage", nullable = false)
    private Integer sequenceInStage;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    // ENUM 정의
    public enum Category {
        writing_study,
        listening_study,
        writing_game,
        listening_game
    }
}
