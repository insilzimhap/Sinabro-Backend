package com.sinabro.backend.user.child.service;

import com.sinabro.backend.user.child.dto.CharacterDto;
import com.sinabro.backend.user.child.dto.CharacterSelectionDto;
import com.sinabro.backend.user.child.entity.CharacterSelection;
import com.sinabro.backend.user.child.repository.CharacterRepository;
import com.sinabro.backend.user.child.repository.CharacterSelectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final CharacterSelectionRepository selectionRepository;

    public CharacterService(CharacterRepository characterRepository, CharacterSelectionRepository selectionRepository) {
        this.characterRepository = characterRepository;
        this.selectionRepository = selectionRepository;
    }

    // 캐릭터 목록 조회
    public List<CharacterDto> getAllCharacters() {
        return characterRepository.findAll().stream()
                .map(c -> {
                    CharacterDto dto = new CharacterDto();
                    dto.setCharacterId(c.getCharacterId());
                    dto.setCharacterName(c.getCharacterName());
                    dto.setImageUrl(c.getImageUrl());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 캐릭터 선택 여부 확인
    public Optional<CharacterSelection> getSelectionByChildId(String childId) {
        return selectionRepository.findByChildId(childId);
    }

    // 캐릭터 선택 저장
    public void selectCharacter(CharacterSelectionDto dto) {
        if (selectionRepository.existsByChildId(dto.getChildId())) {
            throw new IllegalStateException("이미 캐릭터를 선택하셨습니다.");
        }
        CharacterSelection selection = CharacterSelection.builder()
                .childId(dto.getChildId())
                .characterId(dto.getCharacterId())
                .build();
        selectionRepository.save(selection);
    }
}
