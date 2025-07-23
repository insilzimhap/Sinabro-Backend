package com.sinabro.backend.leveltest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class LevelTestOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String optionText; // 보기 텍스트 (예: 사과)

    private String imageUrl;   // 이미지 경로

    private boolean isCorrect; // 정답 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private LevelTestQuestion question;

    // 편한 생성자
    public LevelTestOption(String optionText, String imageUrl, boolean isCorrect) {
        this.optionText = optionText;
        this.imageUrl = imageUrl;
        this.isCorrect = isCorrect;
    }
}
