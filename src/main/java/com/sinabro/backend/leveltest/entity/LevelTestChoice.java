package com.sinabro.backend.leveltest.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "level_test_choices")
public class LevelTestChoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 선택 ID

    @Column(nullable = false)
    private String childId; // 자녀 ID (문자열)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private LevelTestQuestion question; // 문제 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private LevelTestOption option; // 선택한 보기 ID

    @Column(nullable = false)
    private LocalDateTime selectedAt = LocalDateTime.now(); // 선택 시간

    @Column(nullable = false)
    private boolean isCorrect; // 정답 여부

}
