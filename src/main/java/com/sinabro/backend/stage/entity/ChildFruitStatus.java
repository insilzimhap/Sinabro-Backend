package com.sinabro.backend.stage.entity;

import com.sinabro.backend.user.entity.Child;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/**
 * 🌱 자녀별 열매 활성 상태 (Child_Fruit_Status)
 * - Learning_Fruit은 공용 템플릿이며,
 *   자녀별 실제 활성 여부는 본 테이블에서 관리됨.
 * - 한 자녀(child)는 여러 열매(fruit)에 대해 각각 is_active 상태를 가짐.
 */
@Entity
@Table(name = "child_fruit_status")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildFruitStatus {

    // 복합키 (child_id + fruit_id)
    @EmbeddedId
    private ChildFruitStatusId id;

    // 활성 여부 (기본 잠금 상태)
    @Column(name = "is_active", nullable = false)
    private boolean isActive = false;

    // 처음 열린 시각 (자동 기록)
    @CreationTimestamp
    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    // ====== 연관관계 ======

    // 자녀 (FK → child.child_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("childId")
    @JoinColumn(name = "child_id", nullable = false, columnDefinition = "varchar(255)")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Child child;

    // 열매 (FK → learning_fruit.fruit_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("fruitId")
    @JoinColumn(name = "fruit_id", nullable = false, columnDefinition = "varchar(20)")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private LearningFruit learningFruit;
}
