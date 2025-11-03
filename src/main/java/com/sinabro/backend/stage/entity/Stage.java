package com.sinabro.backend.stage.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stage {

    @Id
    @Column(name = "stage_id", length = 10, nullable = false)
    private String stageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Column(name = "level", length = 10, nullable = false)
    private String level;

    // ENUM 정의
    public enum Category {
        writing_study,
        listening_study,
        writing_game,
        listening_game
    }
}
