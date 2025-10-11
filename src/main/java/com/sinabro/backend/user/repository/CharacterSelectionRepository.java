package com.sinabro.backend.user.repository;

import com.sinabro.backend.user.entity.CharacterSelection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * [공용 레포] CharacterSelection (캐릭터 선택 이력)
 * - admin 레포의 메서드 전부 흡수
 */
public interface CharacterSelectionRepository extends JpaRepository<CharacterSelection, Long> {
    // 단건 조회
    Optional<CharacterSelection> findByChildId(String childId);
    // 자녀 존재 여부
    boolean existsByChildId(String childId);

    // admin - 특정 자녀의 최근 선택 1건
    Optional<CharacterSelection> findTopByChildIdOrderByCreatedAtDesc(String childId);

    // admin - 히스토리 조회가 필요하면
    List<CharacterSelection> findByChildIdOrderByCreatedAtDesc(String childId);

    // 자녀 삭제 시 함께 정리
    long deleteByChildId(String childId);

    // 부모 삭제 시 여러 자녀 이력 일괄 삭제
    long deleteByChildIdIn(Collection<String> childIds);
}
