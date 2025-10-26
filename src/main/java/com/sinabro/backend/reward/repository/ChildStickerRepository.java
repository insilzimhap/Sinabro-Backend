package com.sinabro.backend.reward.repository;

import com.sinabro.backend.reward.entity.ChildSticker;
import com.sinabro.backend.reward.entity.ChildStickerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 👦 ChildStickerRepository
 * - 자녀별 스티커 현황 및 업데이트
 */

@Repository
public interface ChildStickerRepository extends JpaRepository<ChildSticker, ChildStickerId> {

    // 특정 자녀 + 스티커 조합 조회
    Optional<ChildSticker> findByChildIdAndStickerId(String childId, String stickerId);

    // 특정 자녀의 전체 스티커 현황 조회
    List<ChildSticker> findByChildId(String childId);

    // 획득 상태 업데이트 (is_obtained = true)
    @Modifying
    @Query("UPDATE ChildSticker cs SET cs.isObtained = true, cs.obtainedAt = CURRENT_TIMESTAMP " +
            "WHERE cs.childId = :childId AND cs.stickerId = :stickerId")
    void updateObtained(@Param("childId") String childId, @Param("stickerId") String stickerId);

    // 자녀가 특정 도감 내에서 획득한 스티커 개수
    @Query("""
           SELECT COUNT(cs)
           FROM ChildSticker cs
           JOIN RewardSticker rs ON cs.stickerId = rs.stickerId
           WHERE cs.childId = :childId AND cs.isObtained = true AND rs.dexId = :dexId
           """)
    int countObtainedByDex(@Param("childId") String childId, @Param("dexId") String dexId);

    // AI 리포트에서 사용
    long countByChildIdAndObtainedAtBetween(String childId, LocalDateTime start, LocalDateTime end);
    long countByChildId(String childId);
}