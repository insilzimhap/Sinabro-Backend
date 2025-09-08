package com.sinabro.backend.study.entity;

import com.sinabro.backend.stage.entity.Stage;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "study_listening_content")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class StudyListeningContent {

    // 문제 ID (PK)
    @Id
    @Column(name = "ls_content_id", length = 255, nullable = false)
    private String lsContentId;

    // DEFAULT '듣기 학습 콘텐츠'
    @Column(
            name = "ls_content_type",
            length = 10,
            nullable = false,
            columnDefinition = "VARCHAR(10) DEFAULT '듣기 학습 콘텐츠'"
    )
    private String lsContentType;

    // ENUM('색상','동물','가족','감정','숫자','일상듣기')
    @Column(
            name = "ls_subject_tag",
            nullable = false,
            columnDefinition = "ENUM('색상','동물','가족','감정','숫자','일상듣기')"
    )
    private String lsSubjectTag;

    // 학습 이미지/오디오 URL (NULL 허용)
    @Column(name = "ls_image_url", length = 255)
    private String lsImageUrl;

    @Column(name = "ls_audio_url", length = 255)
    private String lsAudioUrl;

    // FK → Stage(stage_id), category='listening_study' 조건은 서비스에서 검증
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ls_stage_id", referencedColumnName = "stage_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // Stage 삭제 시 컨텐츠 자동 삭제
    private Stage stage;

    // 해당 단계 내 문제 순서 (1,2,3,4…)
    @Column(name = "ls_content_order", nullable = false)
    private Integer lsContentOrder;
}
