package com.sinabro.backend.game.listening.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;


/**
 * ✅ 듣기 게임 보기 (Option)
 * - 문제(Question)별로 2~4개의 보기 제공
 * - 각 보기는 이미지와 텍스트(선택적)를 가짐
 */

@Entity
@Table(name = "listening_game_option")
@Getter
public class ListeningGameOption {


    // 보기 ID (PK)
    @Id
    @Column(name = "lg_option_id", length = 255, nullable = false)
    private String lgOptionId;

    // 연결 문제 ID (FK → listening_game_question.lg_question_id)
    @Column(name = "lg_question_id", length = 255, nullable = false)
    private String lgQuestionId;

    // 보기 이미지 URL (예: /images/apple.png)
    @Column(name = "lg_image_url", nullable = false)
    private String lgImageUrl;

    // 보기 텍스트 (선택적, 접근성·운영용)
    @Column(name = "lg_option_text")
    private String lgOptionText;

    // 보기 카드 번호 (1~4 등, UI 표시 순서)
    @Column(name = "display_index")
    private Integer displayIndex;

    // 정답 여부 (TRUE=정답, FALSE=오답)
    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    // ====== 연관관계 ======
    // 문제(Question) FK 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_question_id", insertable = false, updatable = false)
    private ListeningGameQuestion listeningGameQuestion;

    // 선택 기록(Choices) — 보기 삭제 시 관련 선택 기록 삭제
    @OneToMany(mappedBy = "listeningGameOption", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<ListeningGameChoices> choices;
}