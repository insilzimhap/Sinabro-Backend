package com.sinabro.backend.stage.dto;

import lombok.Getter;
import java.util.List;

/**
 * 🌳 StageWithFruitsDto
 * - 하나의 스테이지(나무)와 그 안에 속한 모든 열매들의 상태 정보를 담는 DTO
 * - API 응답의 최상위 리스트에 포함되는 요소임
 * - 프론트엔드에서 하나의 '나무' UI를 구성하는 데 사용됨
 */
@Getter
public class StageWithFruitsDto {
    private String stageId;
    private String level;
    private List<FruitStatusDto> fruits;

    public StageWithFruitsDto(String stageId, String level, List<FruitStatusDto> fruits) {
        this.stageId = stageId;
        this.level = level;
        this.fruits = fruits;
    }
}