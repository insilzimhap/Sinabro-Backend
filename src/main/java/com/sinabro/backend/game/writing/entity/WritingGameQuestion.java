package com.sinabro.backend.game.writing.entity;

import com.sinabro.backend.stage.entity.LearningFruit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * ✍️ 쓰기 게임 문제 (Writing_Game_Question)
 * - 한 열매(fruit_id) 안의 개별 문제 단위
 * - 랜덤 출제 대상
 */
@Entity
@Table(
        name = "writing_game_question",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_wgq_fruit_order", columnNames = {"fruit_id", "wg_content_order"})
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WritingGameQuestion {

    // 문제 ID (PK)
    @Id
    @Column(name = "wg_question_id", length = 255, nullable = false)
    private String wgQuestionId;

    // 소속 열매/세트 ID (FK → learning_fruit.fruit_id)
    @Column(name = "fruit_id", length = 20, nullable = false)
    private String fruitId;

    // 문제 태그 (직선/곡선/도형/자음/모음 등)
    @Enumerated(EnumType.STRING)
    @Column(name = "wg_subject_tag", nullable = false, length = 20)
    private WgSubjectTag wgSubjectTag;

    // 정답 텍스트 (자모/단어 등)
    @Column(name = "wg_correct_answer", length = 255, nullable = false)
    private String wgCorrectAnswer;

    // 세트 내 문제 순서 (optional, 랜덤 세트는 NULL 허용)
    @Column(name = "wg_content_order")
    private Integer wgContentOrder;

    // ===== 연관 관계 =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fruit_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private LearningFruit learningFruit;

    // ===== ENUM 정의 =====
    public enum WgSubjectTag {
        직선, 곡선1, 곡선2, 도형,
        자음, 모음, 자모음,
        동물, 과일, 야채, 우리몸
    }
}
