package com.sinabro.backend.study.entity;

import com.sinabro.backend.stage.entity.LearningFruit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "study_writing_content")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "쓰기 학습 콘텐츠 엔티티")
public class StudyWritingContent {

    @Id
    @Column(name = "ws_content_id", length = 255, nullable = false)
    @Schema(description = "쓰기 학습 콘텐츠 ID", example = "W1_LINE_1")
    private String wsContentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fruit_id", referencedColumnName = "fruit_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Schema(description = "소속 열매 ID")
    private LearningFruit fruit;

    @Column(
            name = "ws_subject_tag",
            nullable = false,
            columnDefinition = "ENUM('직선','곡선1','곡선2','도형','자음','이중자음','받침','이중모음','동물','과일','야채','우리몸')"
    )
    @Schema(description = "학습 태그", example = "직선")
    private String wsSubjectTag;

    @Column(name = "ws_content_text", length = 255)
    @Schema(description = "따라쓰기 텍스트", example = "ㄱ")
    private String wsContentText;

    @Column(name = "content_order", nullable = false)
    @Schema(description = "열매 내 순서", example = "1")
    private Integer contentOrder;

    @Column(name = "meta_json", columnDefinition = "TEXT")
    @Schema(description = "메타 정보(JSON)", example = "{\"asset\":\"writing/line1.png\"}")
    private String metaJson;
}
