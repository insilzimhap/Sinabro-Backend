package com.sinabro.backend.reward.service;

import com.sinabro.backend.reward.dto.ChildStickerDto;
import com.sinabro.backend.reward.entity.ChildSticker;
import com.sinabro.backend.reward.entity.ChildStickerId;
//import com.sinabro.backend.reward.entity.RewardSticker;
import com.sinabro.backend.reward.repository.ChildStickerRepository;
import com.sinabro.backend.reward.repository.RewardStickerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardStickerRepository rewardStickerRepository;
    private final ChildStickerRepository childStickerRepository;

    /**
     * 아이가 열매 학습/게임을 완료했을 때 호출되어 스티커를 부여하는 메서드
     */
    @Transactional
    public void grantStickerForFruitCompletion(String childId, String completedFruitId) {
        // 1. 완료한 열매에 해당하는 스티커가 있는지 찾는다.
        rewardStickerRepository.findByFruitId(completedFruitId).ifPresent(sticker -> {

            // 2. 이미 해당 스티커를 획득했는지 확인 (중복 지급 방지)
            ChildStickerId id = new ChildStickerId(childId, sticker.getStickerId());
            if (childStickerRepository.existsById(id)) {
                return; // 이미 있으면 아무것도 안 함
            }

            // 3. 획득 기록 생성 및 저장
            ChildSticker newSticker = ChildSticker.builder()
                    .childId(childId)
                    .stickerId(sticker.getStickerId())
                    .isObtained(true)
                    .obtainedAt(new Timestamp(System.currentTimeMillis()))
                    .build();
            childStickerRepository.save(newSticker);
        });
    }

    /**
     * 특정 자녀가 획득한 모든 스티커 목록을 조회하는 메서드 (프론트엔드 도감 UI용)
     */
    @Transactional(readOnly = true)
    public List<ChildStickerDto> getChildStickers(String childId) {
        List<ChildSticker> stickers = childStickerRepository.findByChildId(childId);
        return stickers.stream()
                .map(sticker -> new ChildStickerDto(
                        sticker.getStickerId(),
                        sticker.getRewardSticker().getStickerName(), // 연관관계 이용
                        sticker.isObtained()
                ))
                .collect(Collectors.toList());
    }
}