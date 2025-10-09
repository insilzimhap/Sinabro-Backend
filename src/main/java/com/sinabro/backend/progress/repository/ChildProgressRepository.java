package com.sinabro.backend.progress.repository;

import com.sinabro.backend.progress.entity.Category;
import com.sinabro.backend.progress.entity.ChildProgress;
import com.sinabro.backend.progress.entity.ChildProgressId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
// JpaRepository<엔티티 클래스, ID 클래스>
// 여기서는 복합 키 클래스인 ChildProgressId를 사용해.
public interface ChildProgressRepository extends JpaRepository<ChildProgress, ChildProgressId> {

    // 특정 아이의 모든 카테고리별 진행도를 조회할 때 사용할 수 있어.
    List<ChildProgress> findByChildId(String childId);

    // 특정 아이의 특정 카테고리 진행도를 조회할 때 사용할 수도 있어. (이미 findById로 가능하지만)
    Optional<ChildProgress> findByChildIdAndCategory(String childId, Category category);
}