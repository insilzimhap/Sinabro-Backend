package com.sinabro.backend.record.entity;

import com.sinabro.backend.stage.entity.LearningFruit; // LearningFruit 엔티티 경로
import com.sinabro.backend.user.entity.Child; // Child 엔티티 경로
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.sql.Timestamp;

@Entity
@Table(name = "listening_game_result")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ListeningGameResult {

    @Id
    @Column(name = "lg_result_id", length = 50, nullable = false)
    private String lgResultId;

    @Builder.Default // Builder 사용 시 기본값을 '듣기 게임'으로 설정
    @Column(name = "result_type", length = 50, nullable = false)
    private String resultType = "듣기 게임";

    @Column(name = "lg_child_id", length = 255, nullable = false)
    private String lgChildId;

    @Column(name = "fruit_id", length = 20, nullable = false)
    private String fruitId;

    @Builder.Default
    @Column(name = "lg_score", nullable = false)
    private int lgScore = 0;

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;

    @Column(name = "is_success", nullable = false)
    private boolean isSuccess;

    @CreationTimestamp // 엔티티가 처음 저장될 때 현재 시간으로 자동 설정
    @Column(name = "lg_play_date", updatable = false)
    private Timestamp lgPlayDate;

    @Column(name = "time_spent_secs")
    private Integer timeSpentSecs;

    @Column(name = "lg_feedback", length = 255)
    private String lgFeedback;

    // --- 연관 관계 매핑 ---

    // ListeningGameResult(N) -> Child(1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_child_id", insertable = false, updatable = false) // lgChildId 컬럼으로 조인
    @OnDelete(action = OnDeleteAction.CASCADE) // 부모(Child)가 삭제되면 자식(이 결과)도 함께 삭제
    private Child child;

    // ListeningGameResult(N) -> LearningFruit(1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fruit_id", insertable = false, updatable = false) // fruitId 컬럼으로 조인
    @OnDelete(action = OnDeleteAction.CASCADE) // 부모(LearningFruit)가 삭제되면 자식(이 결과)도 함께 삭제
    private LearningFruit learningFruit;
}