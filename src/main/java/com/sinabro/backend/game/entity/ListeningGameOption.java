package com.sinabro.backend.game.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "listening_game_option")
@Getter
public class ListeningGameOption {

    @Id
    @Column(name = "lg_option_id", length = 255, nullable = false)
    private String lgOptionId;

    @Column(name = "lg_question_id", length = 255, nullable = false)
    private String lgQuestionId;

    @Column(name = "lg_image_url", nullable = false)
    private String lgImageUrl;

    @Column(name = "lg_option_text")
    private String lgOptionText;

    private Integer displayIndex;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_question_id", insertable = false, updatable = false)
    private ListeningGameQuestion listeningGameQuestion;
}