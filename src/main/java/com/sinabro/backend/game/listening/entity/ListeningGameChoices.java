package com.sinabro.backend.game.listening.entity;

import com.sinabro.backend.record.entity.ListeningGameResult;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/**
 * 🎯 듣기 게임 선택 기록 (문제별 선택 1행)
 * - 한 세션에서 자녀가 선택한 보기 기록
 * - 정답 여부 스냅샷 저장 (Option 변경되어도 기록 유지)
 */
@Entity
@Table(
        name = "listening_game_choices",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_choice", columnNames = {"lg_result_id", "lg_question_id"})
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListeningGameChoices {

    // 선택 기록 ID (PK)
    @Id
    @Column(name = "lg_choice_id", length = 20, nullable = false)
    private String lgChoiceId;

    // 결과 ID (FK → listening_game_result.lg_result_id)
    @Column(name = "lg_result_id", length = 50, nullable = false)
    private String lgResultId;

    // 문제 ID (FK → listening_game_question.lg_question_id)
    @Column(name = "lg_question_id", length = 255, nullable = false)
    private String lgQuestionId;

    // 선택 보기 ID (FK → listening_game_option.lg_option_id)
    @Column(name = "lg_option_id", length = 255, nullable = false)
    private String lgOptionId;

    // 정답 여부 (스냅샷)
    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    // 응답 시각 (DEFAULT CURRENT_TIMESTAMP)
    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    // ====== 연관관계 ======
    // 결과(Result) FK 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_result_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ListeningGameResult listeningGameResult;

    // 문제(Question) FK 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_question_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ListeningGameQuestion listeningGameQuestion;

    // 보기(Option) FK 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_option_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ListeningGameOption listeningGameOption;
}
