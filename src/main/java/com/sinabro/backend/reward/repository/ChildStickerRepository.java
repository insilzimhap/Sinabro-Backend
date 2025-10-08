package com.sinabro.backend.reward.repository;

import com.sinabro.backend.reward.entity.ChildSticker;
import com.sinabro.backend.reward.entity.ChildStickerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChildStickerRepository extends JpaRepository<ChildSticker, ChildStickerId> {

    List<ChildSticker> findByChildId(String childId);

    // AI 리포트에서 사용
    long countByChildIdAndObtainedAtBetween(String childId, LocalDateTime start, LocalDateTime end);
    long countByChildId(String childId);
}