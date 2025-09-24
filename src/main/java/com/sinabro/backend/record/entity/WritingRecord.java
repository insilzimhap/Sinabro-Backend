package com.sinabro.backend.record.entity;

import com.sinabro.backend.stage.entity.LearningFruit;
import com.sinabro.backend.user.child.entity.Child;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Entity
@Table(name = "writing_record")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "쓰기 학습 기록 엔티티")
public class WritingRecord {

    @Id
    @Column(name = "ws_record_id", length = 255, nullable = false)
    @Schema(description = "쓰기 학습 기록 ID", example = "WR2001")
    private String wsRecordId;

    @Column(name = "result_type", nullable = false, length = 50)
    @Schema(description = "결과 타입", example = "쓰기 학습")
    private String resultType;

    @Column(name = "ws_learning_date")
    @Schema(description = "학습 일자", example = "2025-09-16T12:34:56")
    private LocalDateTime wsLearningDate;

    @Column(name = "time_spent_secs")
    @Schema(description = "소요 시간(초)", example = "300")
    private Integer timeSpentSecs;

    @Column(name = "ws_completed", nullable = false)
    @Schema(description = "완료 여부", example = "true")
    private Boolean wsCompleted;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ws_child_id", referencedColumnName = "child_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Schema(description = "자녀 ID")
    private Child child;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fruit_id", referencedColumnName = "fruit_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Schema(description = "대상 열매 ID")
    private LearningFruit fruit;
}
