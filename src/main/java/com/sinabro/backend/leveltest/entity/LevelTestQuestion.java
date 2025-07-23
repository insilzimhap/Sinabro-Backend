package com.sinabro.backend.leveltest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class LevelTestQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int level; // 예: 1, 2, 3

    private String type; // 문제 유형 (예: 듣고 고르기, 호칭 고르기)

    private String prompt; // 질문 문장

    private String questionImageUrl;  // 문제 본문에 띄울 이미지 (예: "가.png")

    private String audioUrl; // 음성 파일 경로 (nullable)

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LevelTestOption> options = new ArrayList<>();

    public void addOption(LevelTestOption option) {
        options.add(option);
        option.setQuestion(this);
    }
}
