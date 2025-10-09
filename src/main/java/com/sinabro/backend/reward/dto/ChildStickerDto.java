package com.sinabro.backend.reward.dto;

import lombok.Getter;

@Getter
public class ChildStickerDto {
    private String stickerId;
    private String stickerName;
    private boolean isObtained;

    public ChildStickerDto(String stickerId, String stickerName, boolean isObtained) {
        this.stickerId = stickerId;
        this.stickerName = stickerName;
        this.isObtained = isObtained;
    }
}