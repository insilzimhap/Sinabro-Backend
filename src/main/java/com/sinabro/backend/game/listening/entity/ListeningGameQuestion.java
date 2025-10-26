package com.sinabro.backend.game.listening.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

/**
 * 💖 듣기 게임 문제 (Question)
 * - 하나의 열매(Fruit) 안에 여러 문제로 구성
 * - 각 문제는 오디오, 이미지, 텍스트, 주제 태그를 포함
 */


@Entity
@Table(name = "listening_game_question")
@Getter
public class ListeningGameQuestion {

    // 문제 ID (PK)
    @Id
    @Column(name = "lg_question_id", length = 255, nullable = false)
    private String lgQuestionId;

    // 소속 열매 ID (FK → learning_fruit.fruit_id)
    @Column(name = "fruit_id", length = 20, nullable = false)
    private String fruitId;

    // 문제 주제 태그 (예: 가족, 색상 등)
    @Column(name = "lg_subject_tag", length = 50, nullable = false)
    private String lgSubjectTag;

    // 세트 내 문제 순서 (UNIQUE(fruit_id, lg_content_order))
    @Column(name = "lg_content_order", nullable = false)
    private int lgContentOrder;

    // 문제 텍스트 (옵션)
    @Column(name = "lg_content_text")
    private String lgContentText;

    // 문제 이미지 URL (옵션)
    @Column(name = "lg_image_url")
    private String lgImageUrl;

    // 보기(Option) 목록 — 문제 삭제 시, 모든 보기 삭제
    @OneToMany(mappedBy = "listeningGameQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<ListeningGameOption> options;
}