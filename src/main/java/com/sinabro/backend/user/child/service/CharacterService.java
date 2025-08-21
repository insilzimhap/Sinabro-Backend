package com.sinabro.backend.user.child.service;

import com.sinabro.backend.user.child.entity.CharacterInfo;
import com.sinabro.backend.user.child.repository.CharacterInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterInfoRepository repo;

    public List<CharacterInfo> listAll() {
        return repo.findAll();
    }

    public String resolveIdByName(String name) {
        return repo.findByCharacterName(name)
                .map(CharacterInfo::getCharacterId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 캐릭터명입니다: " + name));
    }
}
