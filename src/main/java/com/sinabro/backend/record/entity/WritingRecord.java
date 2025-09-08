package com.sinabro.backend.record.entity;

import com.sinabro.backend.study.entity.StudyWritingContent;
import com.sinabro.backend.user.child.entity.Child;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "writing_record")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class WritingRecord {

    // PK
    @Id
    @Column(name = "ws_record_id", length = 255, nullable = false)
    private String wsRecordId;

    // DEFAULT '쓰기 학습'
    @Column(
            name = "result_type",
            length = 50,
            nullable = false,
            columnDefinition = "VARCHAR(50) DEFAULT '쓰기 학습'"
    )
    private String resultType;

    // 학습일자 (DEFAULT CURRENT_TIMESTAMP)
    @Column(
            name = "ws_learning_date",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
    )
    private LocalDateTime wsLearningDate;

    // 소요 시간(분) – INT
    @Column(name = "ws_time_spent")
    private Integer wsTimeSpent;

    // 완료 여부 (DEFAULT false)
    @Column(name = "ws_completed", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean wsCompleted;

    // FK → StudyWritingContent
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ws_content_id", referencedColumnName = "ws_content_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private StudyWritingContent writingContent;

    // FK → Child
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ws_child_id", referencedColumnName = "child_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Child child;
}
