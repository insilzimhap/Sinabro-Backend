package com.sinabro.backend.reward.repository;

import com.sinabro.backend.reward.entity.RewardDex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 🎁 RewardDexRepository
 * - 도감(Reward_Dex) 조회용
 */

@Repository
public interface RewardDexRepository extends JpaRepository<RewardDex, String> {
    // 카테고리별 도감 목록 조회 (예: listening_study / writing_study)
    List<RewardDex> findByCategory(String category);
}