package com.sinabro.backend.user.child.repository;

import com.sinabro.backend.user.child.entity.CharacterInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CharacterInfoRepository extends JpaRepository<CharacterInfo, String> {
    Optional<CharacterInfo> findByCharacterName(String characterName);
}
