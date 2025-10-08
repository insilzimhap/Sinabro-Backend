package com.sinabro.backend.progress.entity;

import com.sinabro.backend.stage.entity.LearningFruit; // LearningFruit 엔티티 경로
import com.sinabro.backend.user.entity.Child; // Child 엔티티 경로
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "child_progress")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@IdClass(ChildProgressId.class) // 복합 키를 사용함을 명시
public class ChildProgress {

    // --- 복합 기본 키 (Composite Primary Key) ---
    @Id
    @Column(name = "child_id", length = 255, nullable = false)
    private String childId;

    @Id
    @Enumerated(EnumType.STRING) // Enum 타입을 DB에 문자열로 저장
    @Column(name = "category", nullable = false)
    private Category category;

    // --- 데이터 컬럼 ---
    @Column(name = "last_fruit_id", length = 20)
    private String lastFruitId;

    @Column(name = "best_fruit_id", length = 20)
    private String bestFruitId;

    @UpdateTimestamp // JPA 엔티티가 업데이트될 때마다 현재 시간으로 자동 설정
    @Column(name = "last_updated_at")
    private Timestamp lastUpdatedAt;

    @Column(name = "best_updated_at")
    private Timestamp bestUpdatedAt;

    // --- 연관 관계 (Foreign Keys) ---
    // 자식(ChildProgress) -> 부모(Child)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", insertable = false, updatable = false) // ID 필드가 이미 있어 중복 삽입/수정 방지
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Child child;

    // 자식(ChildProgress) -> 부모(LearningFruit) for last_fruit_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_fruit_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private LearningFruit lastLearningFruit;

    // 자식(ChildProgress) -> 부모(LearningFruit) for best_fruit_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "best_fruit_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private LearningFruit bestLearningFruit;
}