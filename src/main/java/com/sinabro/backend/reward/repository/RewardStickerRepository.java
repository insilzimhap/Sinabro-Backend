package com.sinabro.backend.reward.repository;

import com.sinabro.backend.reward.entity.RewardSticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 🍬 RewardStickerRepository
 * - Reward_Sticker 테이블 조작
 */

@Repository
public interface RewardStickerRepository extends JpaRepository<RewardSticker, String> {
    // 열매(fruit_id)로 해당 스티커 찾기 (보상 지급 시 사용)
    Optional<RewardSticker> findByFruitId(String fruitId);

    // 도감 ID로 스티커 전체 조회 (도감 페이지 표시용)
    List<RewardSticker> findByDexId(String dexId);
}