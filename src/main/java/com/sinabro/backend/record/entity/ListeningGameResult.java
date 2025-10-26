package com.sinabro.backend.record.entity;

import com.sinabro.backend.stage.entity.LearningFruit; // LearningFruit 엔티티 경로
import com.sinabro.backend.user.entity.Child; // Child 엔티티 경로
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.sql.Timestamp;

/**
 * 🎧 듣기 게임 결과 (열매 1개 세션 단위)
 * - 자녀 1명 기준, 한 열매(Fruit) 플레이 결과를 저장
 * - 정답 수, 총 문항 수, 통과 여부, 소요 시간 포함
 * - Child / LearningFruit와 연관관계 매핑
 */
@Entity
@Table(name = "listening_game_result")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ListeningGameResult {

    // 결과 ID (PK)
    @Id
    @Column(name = "lg_result_id", length = 50, nullable = false)
    private String lgResultId;

    // 결과 타입 (기본값: '듣기 게임')
    @Builder.Default // Builder 사용 시 기본값을 '듣기 게임'으로 설정
    @Column(name = "result_type", length = 50, nullable = false)
    private String resultType = "듣기 게임";

    // 자녀 ID (FK → child.child_id)
    @Column(name = "lg_child_id", length = 255, nullable = false)
    private String lgChildId;

    // 열매 ID (FK → learning_fruit.fruit_id)
    @Column(name = "fruit_id", length = 20, nullable = false)
    private String fruitId;

    // 정답 개수 (기본값 0)
    @Builder.Default
    @Column(name = "lg_score", nullable = false)
    private int lgScore = 0;

    // 전체 문항 수 (예: 4)
    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;

    // 통과 여부 (3개 이상 정답 = TRUE)
    @Column(name = "is_success", nullable = false)
    private boolean isSuccess;

    // 플레이 일시 (엔티티 저장 시 자동 생성)
    @CreationTimestamp // 엔티티가 처음 저장될 때 현재 시간으로 자동 설정
    @Column(name = "lg_play_date", updatable = false)
    private Timestamp lgPlayDate;

    // 소요 시간 (초 단위)
    @Column(name = "time_spent_secs")
    private Integer timeSpentSecs;

    // 피드백 (선택 입력, NULL 허용)
    @Column(name = "lg_feedback", length = 255)
    private String lgFeedback;

    // --- 연관 관계 매핑 ---

    // ListeningGameResult(N) -> Child(1)
    // 자녀(Child) FK 매핑 (읽기 전용)
    // - 부모(Child)가 삭제되면 자식(결과)도 함께 삭제됨
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_child_id", insertable = false, updatable = false) // lgChildId 컬럼으로 조인
    @OnDelete(action = OnDeleteAction.CASCADE) // 부모(Child)가 삭제되면 자식(이 결과)도 함께 삭제
    private Child child;

    // ListeningGameResult(N) -> LearningFruit(1)
    // 열매(Fruit) FK 매핑 (읽기 전용)
    // - 부모(LearningFruit)가 삭제되면 자식(결과)도 함께 삭제됨
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fruit_id", insertable = false, updatable = false) // fruitId 컬럼으로 조인
    @OnDelete(action = OnDeleteAction.CASCADE) // 부모(LearningFruit)가 삭제되면 자식(이 결과)도 함께 삭제
    private LearningFruit learningFruit;
}