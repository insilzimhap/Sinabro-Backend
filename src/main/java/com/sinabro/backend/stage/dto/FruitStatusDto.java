package com.sinabro.backend.stage.dto;

import lombok.Getter;

/**
 * 🍓 FruitStatusDto
 * - 개별 열매의 상태 정보를 담는 DTO (Data Transfer Object)
 * - StageWithFruitsDto 내부에 리스트 형태로 포함됨
 * - 프론트엔드에서 열매 하나의 UI(활성/비활성)를 그리는 데 사용됨
 */
@Getter
public class FruitStatusDto {
    private String fruitId;
    private String title;
    private int sequenceInStage;
    private boolean isActive;

    public FruitStatusDto(String fruitId, String title, int sequenceInStage, boolean isActive) {
        this.fruitId = fruitId;
        this.title = title;
        this.sequenceInStage = sequenceInStage;
        this.isActive = isActive;
    }
}