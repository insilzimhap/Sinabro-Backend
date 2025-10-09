package com.sinabro.backend.reward.repository;

import com.sinabro.backend.reward.entity.RewardDex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RewardDexRepository extends JpaRepository<RewardDex, String> {
}