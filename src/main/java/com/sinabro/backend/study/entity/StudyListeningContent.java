package com.sinabro.backend.study.entity;

import com.sinabro.backend.stage.entity.LearningFruit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "study_listening_content")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "듣기 학습 콘텐츠 엔티티")
public class StudyListeningContent {

    @Id
    @Column(name = "ls_content_id", length = 255, nullable = false)
    @Schema(description = "듣기 학습 콘텐츠 ID", example = "L1_COLOR_RED")
    private String lsContentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fruit_id", referencedColumnName = "fruit_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Schema(description = "소속 열매 ID")
    private LearningFruit fruit;

    @Column(
            name = "ls_situation_tag",
            nullable = false,
            columnDefinition = "ENUM('색상','동물','가족','감정','숫자','일상듣기')"
    )
    @Schema(description = "학습 주제 태그", example = "색상")
    private String lsSituationTag;

    @Column(name = "content_order", nullable = false)
    @Schema(description = "열매 내 순서", example = "1")
    private Integer contentOrder;

    @Column(name = "meta_json", columnDefinition = "TEXT")
    @Schema(description = "메타 정보(JSON)", example = "{\"asset\":\"color/red.png\"}")
    private String metaJson;
}
