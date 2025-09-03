package com.sinabro.backend.study.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import com.sinabro.backend.stage.entity.Stage;

@Entity
@Table(name = "study_writing_content")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class StudyWritingContent {

    // 문제 ID (PK)
    @Id
    @Column(name = "ws_content_id", length = 255, nullable = false)
    private String wsContentId;

    // ENUM(12종) – DB 레벨 제약
    @Column(
            name = "ws_subject_tag",
            nullable = false,
            columnDefinition =
                    "ENUM('직선','곡선1','곡선2','도형','자음','이중자음','받침','이중모음','동물','과일','야채','우리 몸')"
    )
    private String wsSubjectTag;

    // 기본값 '쓰기 학습 콘텐츠'
    @Column(
            name = "ws_content_type",
            length = 10,
            nullable = false,
            columnDefinition = "VARCHAR(10) DEFAULT '쓰기 학습 콘텐츠'"
    )
    private String wsContentType;

    // 따라쓰기 텍스트 (NOT NULL)
    @Column(name = "ws_content_text", length = 255, nullable = false)
    private String wsContentText;

    // 보조 이미지/오디오/스트로크 이미지 (NULL 허용)
    @Column(name = "ws_image_url", length = 255)
    private String wsImageUrl;

    @Column(name = "ws_audio_url", length = 255)
    private String wsAudioUrl;

    @Column(name = "ws_stroke_image_url", length = 255)
    private String wsStrokeImageUrl;

    // FK → Stage(stage_id), category='writing_study' 조건은 서비스에서 검증
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ws_stage_id", referencedColumnName = "stage_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Stage stage;

    // 해당 단계 내 문제 순서
    @Column(name = "ws_content_order", nullable = false)
    private Integer wsContentOrder;
}
