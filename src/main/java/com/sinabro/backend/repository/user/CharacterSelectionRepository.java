package com.sinabro.backend.repository.user;

import com.sinabro.backend.entity.user.CharacterSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CharacterSelectionRepository extends JpaRepository<CharacterSelection, Long> {
    Optional<CharacterSelection> findByChildId(String childId);
    boolean existsByChildId(String childId); // ✅ 반드시 추가!

}
