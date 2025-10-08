package com.sinabro.backend.game.service;

import com.sinabro.backend.game.dto.GameResultDto;
import com.sinabro.backend.record.entity.ListeningGameResult;
import com.sinabro.backend.record.repository.ListeningGameResultRepository;
import com.sinabro.backend.reward.service.RewardService;
import com.sinabro.backend.user.entity.Child;
import com.sinabro.backend.user.repository.ChildRepository;
import com.sinabro.backend.weakness.service.ChildWeaknessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameService {

    private final ListeningGameResultRepository listeningGameResultRepository;
    private final ChildRepository childRepository;
    private final ChildWeaknessService childWeaknessService;
    private final RewardService rewardService;

    @Transactional
    public void processListeningGameResult(GameResultDto dto) {
        // 1. 게임 결과(Result) 엔티티 생성 및 DB 저장
        ListeningGameResult result = ListeningGameResult.builder()
                .lgResultId("lg-res-" + UUID.randomUUID()) // 결과 ID 랜덤 생성
                .lgChildId(dto.getChildId())
                .fruitId(dto.getFruitId())
                .lgScore(dto.getScore())
                .totalQuestions(dto.getTotalQuestions())
                .isSuccess(dto.isSuccess())
                .timeSpentSecs(dto.getTimeSpentSecs())
                .build();
        listeningGameResultRepository.save(result);

        // 2. 자녀 엔티티 조회
        Child child = childRepository.findById(dto.getChildId())
                .orElseThrow(() -> new RuntimeException("Child not found"));

        // 3. 취약점 분석 서비스 호출
        childWeaknessService.analyzeAndUpsertWeakness(child, dto.getFruitId());

        // 4. 게임 성공 시, 보상(스티커) 지급 서비스 호출
        if (dto.isSuccess()) {
            rewardService.grantStickerForFruitCompletion(dto.getChildId(), dto.getFruitId());
        }
    }
}