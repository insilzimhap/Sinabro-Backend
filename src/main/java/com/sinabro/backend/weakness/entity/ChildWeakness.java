package com.sinabro.backend.weakness.entity;

import com.sinabro.backend.progress.entity.Category;
import com.sinabro.backend.user.entity.Child;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "child_weakness")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ChildWeakness {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long weaknessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Child child;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Category category; // Category Enum은 progress 패키지에 있는 걸 같이 쓰면 돼.

    @Column(length = 50, nullable = false)
    private String subjectTag;

    @Column(precision = 5, scale = 2)
    private BigDecimal weaknessScore;

    @Lob // TEXT 타입 매핑
    private String analysisText;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp analyzedAt;
}