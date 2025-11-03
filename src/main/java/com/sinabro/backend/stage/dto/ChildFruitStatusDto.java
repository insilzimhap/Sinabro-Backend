package com.sinabro.backend.stage.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * [자녀별 열매 활성 상태 DTO]
 * - Child_Fruit_Status 테이블 데이터를 조회하거나 전송할 때 사용
 * - 각 자녀가 어떤 열매를 열었는지(is_active)와 열린 시점을 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildFruitStatusDto {

    private String childId;       // 자녀 ID (Child.child_id)
    private String fruitId;       // 열매 ID (Learning_Fruit.fruit_id)
    private boolean isActive;     // 활성화 여부 (Child_Fruit_Status.is_active)
    private LocalDateTime openedAt; // 첫 활성(오픈) 시각 (Child_Fruit_Status.opened_at)
}
