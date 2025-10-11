package com.sinabro.backend.reward.controller;

import com.sinabro.backend.reward.dto.ChildStickerDto;
import com.sinabro.backend.reward.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    @GetMapping("/stickers/{childId}")
    public ResponseEntity<List<ChildStickerDto>> getChildStickers(@PathVariable String childId) {
        List<ChildStickerDto> stickers = rewardService.getChildStickers(childId);
        return ResponseEntity.ok(stickers);
    }
}