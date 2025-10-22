package com.sinabro.backend.record.entity;

import com.sinabro.backend.stage.entity.LearningFruit;
import com.sinabro.backend.user.entity.Child;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.sql.Timestamp;

/**
 * 📘 쓰기 게임 결과 (Writing_Game_Result)
 * - 한 세션(=열매 1개) 단위의 총 결과 저장
 * - 정답 수, 전체 문항 수, 통과 여부, 소요 시간 등 포함
 * - 게임 재도전 시 매번 새 row insert (update 아님)
 */
@Entity
@Table(name = "writing_game_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WritingGameResult {

    // 결과 ID (PK)
    @Id
    @Column(name = "wg_result_id", length = 50, nullable = false)
    private String wgResultId;

    // 결과 타입 (고정: "쓰기 게임")
    @Builder.Default
    @Column(name = "result_type", length = 50, nullable = false)
    private String resultType = "쓰기 게임";

    // 자녀 ID (FK → child.child_id)
    @Column(name = "wg_child_id", length = 255, nullable = false)
    private String wgChildId;

    // 대상 열매 ID (FK → learning_fruit.fruit_id)
    @Column(name = "fruit_id", length = 20, nullable = false)
    private String fruitId;

    // 정답 수 (맞춘 문제 개수)
    @Builder.Default
    @Column(name = "wg_score", nullable = false)
    private int wgScore = 0;

    // 전체 문항 수
    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;

    // 통과 여부 (3개 이상 정답 = true)
    @Column(name = "is_success", nullable = false)
    private boolean isSuccess;

    // 플레이 일시 (기본값: CURRENT_TIMESTAMP)
    @CreationTimestamp
    @Column(name = "wg_play_date", updatable = false)
    private Timestamp wgPlayDate;

    // 소요 시간 (초 단위)
    @Column(name = "time_spent_secs")
    private Integer timeSpentSecs;

    // 피드백 (선택 입력)
    @Column(name = "wg_feedback", length = 255)
    private String wgFeedback;

    // ===== 연관 관계 =====

    // 자녀(Child) FK 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wg_child_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Child child;

    // 열매(Fruit) FK 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fruit_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private LearningFruit learningFruit;
}
