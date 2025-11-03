package com.sinabro.backend.study.entity;

import com.sinabro.backend.stage.entity.LearningFruit;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "study_writing_content",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_ws_fruit_order", columnNames = {"fruit_id", "content_order"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyWritingContent {

    @Id
    @Column(name = "ws_content_id", length = 255, nullable = false)
    private String wsContentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fruit_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ws_content_fruit"))
    private LearningFruit fruit;

    @Enumerated(EnumType.STRING)
    @Column(name = "ws_subject_tag", nullable = false)
    private WsSubjectTag wsSubjectTag;

    @Column(name = "ws_content_text", length = 255)
    private String wsContentText;

    @Column(name = "content_order", nullable = false)
    private Integer contentOrder;

    @Lob
    @Column(name = "meta_json")
    private String metaJson;

    @Column(name = "ws_audio_url", length = 255)
    private String wsAudioUrl;

    @Column(name = "ws_content_order", nullable = false)
    private Integer wsContentOrder;

    @Column(name = "ws_content_type", length = 10, nullable = false)
    private String wsContentType = "쓰기 학습 콘텐츠";

    @Column(name = "ws_image_url", length = 255)
    private String wsImageUrl;

    @Column(name = "ws_stroke_image_url", length = 255)
    private String wsStrokeImageUrl;

    @Column(name = "ws_stage_id", length = 10, nullable = false)
    private String wsStageId;

    // ENUM 정의
    public enum WsSubjectTag {
        직선, 곡선1, 곡선2, 도형, 자음, 모음, 동물, 과일, 야채, 우리몸
    }
}
