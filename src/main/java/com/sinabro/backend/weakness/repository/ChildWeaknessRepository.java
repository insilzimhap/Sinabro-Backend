package com.sinabro.backend.weakness.repository;

import com.sinabro.backend.user.entity.Child;
import com.sinabro.backend.weakness.entity.ChildWeakness;
import com.sinabro.backend.progress.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChildWeaknessRepository extends JpaRepository<ChildWeakness, Long> {

    // UPSERT를 위해 기존 기록을 찾는 메서드
    Optional<ChildWeakness> findByChildAndCategoryAndSubjectTag(Child child, Category category, String subjectTag);
}