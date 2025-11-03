package com.sinabro.backend.record.entity;

import com.sinabro.backend.stage.entity.LearningFruit;
import com.sinabro.backend.study.entity.StudyWritingContent;
import com.sinabro.backend.user.entity.Child;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.sql.Timestamp;

@Entity
@Table(name = "writing_record")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class WritingRecord {

    @Id
    @Column(name = "ws_record_id", length = 255, nullable = false)
    private String wsRecordId;

    @Column(name = "result_type", length = 50, nullable = false)
    private String resultType = "쓰기 학습";

    @CreationTimestamp
    @Column(name = "ws_learning_date")
    private Timestamp wsLearningDate;

    @Column(name = "time_spent_secs")
    private Integer timeSpentSecs;

    @Column(name = "ws_completed", nullable = false)
    private boolean wsCompleted = false;

    @Column(name = "ws_child_id", length = 255, nullable = false)
    private String wsChildId;

    @Column(name = "fruit_id", length = 20, nullable = false)
    private String fruitId;

    @Column(name = "ws_content_id", length = 255, nullable = false)
    private String wsContentId;

    // --- 연관 관계 ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ws_child_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Child child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fruit_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private LearningFruit learningFruit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ws_content_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private StudyWritingContent studyWritingContent;
}