package com.sinabro.backend.reward.service;


import com.sinabro.backend.reward.entity.ChildSticker;
import com.sinabro.backend.reward.repository.ChildStickerRepository;
import com.sinabro.backend.reward.repository.RewardStickerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import com.sinabro.backend.reward.dto.*;
import com.sinabro.backend.reward.entity.*;
import com.sinabro.backend.reward.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;



/**
 * RewardService
 * - 학습 완료 후 보상(스티커) 지급 및 조회 관련 주요 비즈니스 로직 담당
 *   (giveSticker / getStickerStatus / getDexSummary)
 */

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RewardService {

    private final RewardDexRepository rewardDexRepository;
    private final RewardStickerRepository rewardStickerRepository;
    private final ChildStickerRepository childStickerRepository;

    //================== 🎁 보상 지급 (학습 완료 후 호출) ==================

    /**
     * 스티커 지급
     * - 입력 DTO: RewardStickerRequestDto
     * - 동작:
     *   1) fruitId → RewardSticker 매핑 조회
     *   2) Child_Sticker에서 자녀의 기존 기록 조회
     *   3) 이미 is_obtained=true → PASS
     *   4) false 또는 신규 → is_obtained=true로 INSERT/UPDATE
     *
     * 성공 시 → RewardStickerResponseDto 반환
     */
    public RewardStickerResponseDto giveSticker(RewardStickerRequestDto req) {
        String childId = req.getChildId();
        String fruitId = req.getFruitId();

        log.info("[RewardService][giveSticker] 보상 지급 요청 childId={} fruitId={}", childId, fruitId);

        // 1️⃣ 열매로 스티커 찾기
        RewardSticker sticker = rewardStickerRepository.findByFruitId(fruitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 스티커 정의를 찾을 수 없습니다."));

        // 2️⃣ 자녀의 기존 스티커 기록 조회
        Optional<ChildSticker> existingOpt = childStickerRepository.findByChildIdAndStickerId(childId, sticker.getStickerId());

        // 이미 획득한 경우 PASS
        if (existingOpt.isPresent() && Boolean.TRUE.equals(existingOpt.get().getIsObtained())) {
            log.info("[RewardService][giveSticker] 이미 획득한 스티커 - childId={} stickerId={}", childId, sticker.getStickerId());
            return RewardStickerResponseDto.builder()
                    .success(false)
                    .message("이미 획득한 스티커입니다.")
                    .stickerId(sticker.getStickerId())
                    .stickerName(sticker.getStickerName())
                    .obtainedAt(existingOpt.get().getObtainedAt().toLocalDateTime())
                    .build();
        }

        // 3️⃣ 신규 저장 또는 false → true 업데이트
        if (existingOpt.isPresent()) {
            childStickerRepository.updateObtained(childId, sticker.getStickerId());
            log.info("[RewardService][giveSticker] 기존 레코드 업데이트 → isObtained=true");
        } else {
            ChildSticker newEntity = ChildSticker.builder()
                    .childId(childId)
                    .stickerId(sticker.getStickerId())
                    .isObtained(true)
                    .obtainedAt(java.sql.Timestamp.valueOf(LocalDateTime.now()))
                    .build();
            childStickerRepository.save(newEntity);
            log.info("[RewardService][giveSticker] 새 스티커 획득 childId={} stickerId={}", childId, sticker.getStickerId());
        }

        // ✅ 반환 DTO 구성
        return RewardStickerResponseDto.builder()
                .success(true)
                .message("스티커 획득 완료")
                .stickerId(sticker.getStickerId())
                .stickerName(sticker.getStickerName())
                .obtainedAt(LocalDateTime.now())
                .build();
    }

    //================== 👦 자녀별 스티커 현황 조회 ==================

    /**
     * 스티커 현황 조회
     * - 입력: childId
     * - 동작:
     *   1) Reward_Sticker 전체 목록 조회
     *   2) Child_Sticker에서 해당 자녀의 획득 상태 조회
     *   3) 병합하여 각 스티커별 획득 여부 반환
     */
    @Transactional(readOnly = true)
    public List<RewardStickerStatusDto> getStickerStatus(String childId) {
        log.info("[RewardService][getStickerStatus] 스티커 현황 조회 childId={}", childId);

        // 전체 스티커 목록
        List<RewardSticker> allStickers = rewardStickerRepository.findAll();

        // 자녀별 획득 여부 map
        Map<String, Boolean> obtainedMap = childStickerRepository.findByChildId(childId)
                .stream()
                .collect(Collectors.toMap(ChildSticker::getStickerId, ChildSticker::getIsObtained));

        // DTO 변환
        return allStickers.stream().map(s -> RewardStickerStatusDto.builder()
                        .stickerId(s.getStickerId())
                        .stickerName(s.getStickerName())
                        .dexId(s.getDexId())
                        .sequenceInDex(s.getSequenceInDex())
                        .isObtained(obtainedMap.getOrDefault(s.getStickerId(), false))
                        .build())
                .sorted(Comparator.comparing(RewardStickerStatusDto::getDexId)
                        .thenComparing(RewardStickerStatusDto::getSequenceInDex))
                .collect(Collectors.toList());
    }

    //================== 📘 도감별 요약 (진행도) ==================

    /**
     * 도감 요약 조회
     * - 입력: childId
     * - 동작:
     *   1) Reward_Dex 전체 조회
     *   2) 각 도감별로 획득한 스티커 개수 계산 (JOIN)
     *   3) 총 개수/획득 개수 DTO 반환
     */
    @Transactional(readOnly = true)
    public List<RewardDexSummaryDto> getDexSummary(String childId) {
        log.info("[RewardService][getDexSummary] 도감 요약 조회 childId={}", childId);

        List<RewardDex> allDex = rewardDexRepository.findAll();
        List<RewardDexSummaryDto> result = new ArrayList<>();

        for (RewardDex dex : allDex) {
            int obtained = childStickerRepository.countObtainedByDex(childId, dex.getDexId());
            result.add(RewardDexSummaryDto.builder()
                    .dexId(dex.getDexId())
                    .dexName(dex.getDexName())
                    .category(dex.getCategory().name())
                    .totalStickers(dex.getTotalStickers())
                    .obtainedCount(obtained)
                    .build());
        }

        log.info("[RewardService][getDexSummary] 도감 {}건 요약 완료 childId={}", result.size(), childId);
        return result;
    }
}