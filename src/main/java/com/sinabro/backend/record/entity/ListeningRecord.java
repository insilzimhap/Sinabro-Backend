package com.sinabro.backend.record.entity;

import com.sinabro.backend.study.entity.StudyListeningContent;
import com.sinabro.backend.user.child.entity.Child;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "listening_record")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ListeningRecord {

    // PK
    @Id
    @Column(name = "ls_record_id", length = 255, nullable = false)
    private String lsRecordId;

    // DEFAULT '듣기 학습'
    @Column(
            name = "result_type",
            length = 50,
            nullable = false,
            columnDefinition = "VARCHAR(50) DEFAULT '듣기 학습'"
    )
    private String resultType;

    // 학습일자 (DEFAULT CURRENT_TIMESTAMP)
    @Column(
            name = "ls_learning_date",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
    )
    private LocalDateTime lsLearningDate;

    // 소요 시간 (분) – TIME 사용
    @Column(name = "ls_time_spent")
    private LocalTime lsTimeSpent;

    // 완료 여부 (DEFAULT false)
    @Column(name = "ls_completed", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean lsCompleted;

    // FK → StudyListeningContent
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ls_content_id", referencedColumnName = "ls_content_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private StudyListeningContent listeningContent;

    // FK → Child
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ls_child_id", referencedColumnName = "child_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Child child;
}
