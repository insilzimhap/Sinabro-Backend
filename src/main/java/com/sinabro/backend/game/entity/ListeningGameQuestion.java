package com.sinabro.backend.game.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "listening_game_question")
@Getter
public class ListeningGameQuestion {

    @Id
    @Column(name = "lg_question_id", length = 255, nullable = false)
    private String lgQuestionId;

    @Column(name = "fruit_id", length = 20, nullable = false)
    private String fruitId;

    @Column(name = "lg_subject_tag", length = 50, nullable = false)
    private String lgSubjectTag;

    @Column(name = "lg_content_order", nullable = false)
    private int lgContentOrder;

    @Column(name = "lg_content_text")
    private String lgContentText;

    @Column(name = "lg_image_url")
    private String lgImageUrl;
}