package com.sinabro.backend.user.repository;

import com.sinabro.backend.user.entity.CharacterInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** [공용 레포] CharacterInfo (캐릭터 메타)
 *  - 기존 커스텀 쿼리 유지 (byCharacterName 등)
 */
public interface CharacterInfoRepository extends JpaRepository<CharacterInfo, String> {
    //
    Optional<CharacterInfo> findByCharacterName(String characterName);
}
