package com.sinabro.backend.stage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * 🔑 Child_Fruit_Status 복합키
 * - child_id + fruit_id 복합 기본키 구성
 * - 자녀별 열매 상태를 유일하게 식별
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class ChildFruitStatusId implements Serializable {

    // 자녀 ID (FK → child.child_id)
    @Column(name = "child_id", length = 255, nullable = false)
    private String childId;

    // 열매 ID (FK → learning_fruit.fruit_id)
    @Column(name = "fruit_id", length = 20, nullable = false)
    private String fruitId;
}
