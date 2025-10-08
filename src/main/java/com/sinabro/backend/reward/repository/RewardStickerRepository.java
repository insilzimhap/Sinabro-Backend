package com.sinabro.backend.reward.repository;

import com.sinabro.backend.reward.entity.RewardSticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RewardStickerRepository extends JpaRepository<RewardSticker, String> {
    // 열매 완료 시 스티커를 찾기 위한 메서드
    Optional<RewardSticker> findByFruitId(String fruitId);
}