package com.sinabro.backend.reward.controller;

import com.sinabro.backend.reward.dto.*;
import com.sinabro.backend.reward.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;



import java.util.List;

/**
 * [앱 > 보상(스티커/도감) API]
 * - POST   /api/app/reward/sticker       : 스티커 지급 (학습 완료 후 호출)
 * - GET    /api/app/reward/sticker       : 자녀별 스티커 현황 조회
 * - GET    /api/app/reward/dex           : 도감 요약 조회 (도감별 진행률)
 *
 * ※ 인증 연동 시 childId는 JWT 주체 기반으로 자동 추출 가능
 */
@RestController
@RequestMapping("/api/app/reward")
@RequiredArgsConstructor
@Slf4j
public class RewardController {

    private final RewardService rewardService;

    /** 🎁 스티커 지급 (학습 완료 후 호출) */
    @PostMapping("/sticker")
    public ResponseEntity<RewardStickerResponseDto> giveSticker(
            @RequestBody RewardStickerRequestDto req
    ) {
        log.info("[RewardController][giveSticker] 호출 childId={} fruitId={}",
                req.getChildId(), req.getFruitId());
        var dto = rewardService.giveSticker(req);
        return ResponseEntity.ok(dto);
    }

    /** 👦 자녀별 스티커 현황 조회 */
    @GetMapping("/sticker")
    public ResponseEntity<List<RewardStickerStatusDto>> getStickerStatus(
            @RequestParam String childId
    ) {
        log.info("[RewardController][getStickerStatus] 호출 childId={}", childId);
        var list = rewardService.getStickerStatus(childId);
        return ResponseEntity.ok(list);
    }

    /** 📘 도감 요약 조회 (도감별 획득 개수 / 총 개수 -> UI용) */
    @GetMapping("/dex")
    public ResponseEntity<List<RewardDexSummaryDto>> getDexSummary(
            @RequestParam String childId
    ) {
        log.info("[RewardController][getDexSummary] 호출 childId={}", childId);
        var list = rewardService.getDexSummary(childId);
        return ResponseEntity.ok(list);
    }
}