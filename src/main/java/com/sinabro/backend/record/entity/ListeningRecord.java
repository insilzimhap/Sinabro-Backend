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
@Table(name = "listening_record")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ListeningRecord {

    @Id
    @Column(name = "ls_record_id", length = 255, nullable = false)
    private String lsRecordId;

    @Column(name = "result_type", length = 50, nullable = false)
    private String resultType = "듣기 학습";

    @CreationTimestamp
    @Column(name = "ls_learning_date")
    private Timestamp lsLearningDate;

    @Column(name = "time_spent_secs")
    private Integer timeSpentSecs;

    @Column(name = "ls_completed", nullable = false)
    private boolean lsCompleted = false;

    // ✅ 이 필드 이름이 'lsChildId' 이어야 해!
    @Column(name = "ls_child_id", length = 255, nullable = false)
    private String lsChildId;

    @Column(name = "fruit_id", length = 20, nullable = false)
    private String fruitId;

    @Column(name = "ls_content_id", length = 255, nullable = false)
    private String lsContentId;

    // --- 연관 관계 ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ls_child_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Child child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fruit_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private LearningFruit learningFruit;
}