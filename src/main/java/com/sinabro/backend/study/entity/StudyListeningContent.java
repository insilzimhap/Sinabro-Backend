package com.sinabro.backend.study.entity;

import com.sinabro.backend.stage.entity.LearningFruit;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "study_listening_content",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_fruit_content", columnNames = {"fruit_id", "content_order"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyListeningContent {

    @Id
    @Column(name = "ls_content_id", length = 255, nullable = false)
    private String lsContentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fruit_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ls_content_fruit"))
    private LearningFruit fruit;

    @Enumerated(EnumType.STRING)
    @Column(name = "ls_subject_tag", nullable = false)
    private SubjectTag lsSubjectTag;

    @Column(name = "content_order", nullable = false)
    private Integer contentOrder;

    @Lob
    @Column(name = "meta_json")
    private String metaJson;

    @Column(name = "ls_audio_url", length = 255)
    private String lsAudioUrl;

    @Column(name = "ls_content_order", nullable = false)
    private Integer lsContentOrder;

    @Column(name = "ls_content_type", length = 10, nullable = false)
    private String lsContentType = "듣기 학습 콘텐츠";

    @Column(name = "ls_image_url", length = 255)
    private String lsImageUrl;

    @Column(name = "ls_stage_id", length = 10, nullable = false)
    private String lsStageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ls_situation_tag", nullable = false)
    private SituationTag lsSituationTag;

    // ENUM 정의
    public enum SubjectTag {
        색상, 동물, 가족, 감정, 숫자, 일상듣기
    }

    public enum SituationTag {
        색상, 동물, 가족, 감정, 숫자, 일상듣기
    }
}
