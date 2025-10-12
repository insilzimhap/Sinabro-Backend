package com.sinabro.backend.game.writing.entity;

import com.sinabro.backend.record.entity.WritingGameResult;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

/**
 * 🧾 쓰기 게임 선택 기록 (Writing_Game_Choices)
 * - 한 세션(resultId) 내의 문제별 기록
 * - 필기 인식 결과(child_written_text)와 정답 여부 스냅샷 저장
 */
@Entity
@Table(
        name = "writing_game_choices",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_wgc_result_question", columnNames = {"wg_result_id", "wg_question_id"})
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WritingGameChoices {

    // 선택 기록 ID (PK)
    @Id
    @Column(name = "wg_choice_id", length = 20, nullable = false)
    private String wgChoiceId;

    // 세션 결과 ID (FK → writing_game_result.wg_result_id)
    @Column(name = "wg_result_id", length = 20, nullable = false)
    private String wgResultId;

    // 문제 ID (FK → writing_game_question.wg_question_id)
    @Column(name = "wg_question_id", length = 255, nullable = false)
    private String wgQuestionId;

    // 자녀가 쓴 텍스트 (필기인식 1순위 결과)
    @Column(name = "child_written_text", length = 255)
    private String childWrittenText;

    // 정답 여부 (스냅샷)
    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    // 응답 시각
    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    // ===== 연관 관계 =====
    // 결과(Result) FK 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wg_result_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private WritingGameResult writingGameResult;

    // 문제(Question) FK 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wg_question_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private WritingGameQuestion writingGameQuestion;
}
