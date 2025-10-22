package com.sinabro.backend.record.entity;

import com.sinabro.backend.stage.entity.LearningFruit;
import com.sinabro.backend.user.entity.Child;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.sql.Timestamp;

@Entity
@Table(name = "writing_game_result")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class WritingGameResult {

    @Id
    @Column(name = "wg_result_id", length = 50, nullable = false)
    private String wgResultId;

    @Builder.Default
    @Column(name = "result_type", length = 50, nullable = false)
    private String resultType = "쓰기 게임";

    @Column(name = "wg_child_id", length = 255, nullable = false)
    private String wgChildId;

    @Column(name = "fruit_id", length = 20, nullable = false)
    private String fruitId;

    @Builder.Default
    @Column(name = "wg_score", nullable = false)
    private int wgScore = 0;

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;

    @Column(name = "is_success", nullable = false)
    private boolean isSuccess;

    @CreationTimestamp
    @Column(name = "wg_play_date", updatable = false)
    private Timestamp wgPlayDate;

    @Column(name = "time_spent_secs")
    private Integer timeSpentSecs;

    @Column(name = "wg_feedback", length = 255)
    private String wgFeedback;

    // --- 연관 관계 매핑 ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wg_child_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Child child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fruit_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private LearningFruit learningFruit;
}