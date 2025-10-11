package com.sinabro.backend.stage.entity;

import com.sinabro.backend.progress.entity.Category;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "learning_fruit")
@Getter
public class LearningFruit {

    @Id
    @Column(name = "fruit_id", length = 20, nullable = false)
    private String fruitId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    // ✅ 이 필드를 추가해줘!
    @Column(name = "stage_id", length = 10, nullable = false)
    private String stageId;

    @Column(name = "sequence_in_stage")
    private int sequenceInStage;

    @Column(length = 100)
    private String title;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}