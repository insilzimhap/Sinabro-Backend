package com.sinabro.backend.user.child.controller;

import com.sinabro.backend.user.child.dto.CharacterDto;
import com.sinabro.backend.user.child.dto.CharacterSelectionDto;
import com.sinabro.backend.user.child.entity.CharacterSelection;
import com.sinabro.backend.user.child.service.CharacterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/character")
public class CharacterController {

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    // (1) 캐릭터 목록 조회
    @GetMapping("/list")
    public List<CharacterDto> getCharacters() {
        return characterService.getAllCharacters();
    }

    // (2) 캐릭터 선택 여부 확인
    @GetMapping("/selection/check")
    public Map<String, Object> checkSelected(@RequestParam("childId") String childId) {
        Optional<CharacterSelection> selection = characterService.getSelectionByChildId(childId);
        Map<String, Object> result = new HashMap<>();
        result.put("selected", selection.isPresent());
        result.put("characterId", selection.map(CharacterSelection::getCharacterId).orElse(null));
        return result;
    }

    // (3) 캐릭터 선택(저장)
    @PostMapping("/selection")
    public ResponseEntity<?> selectCharacter(@RequestBody CharacterSelectionDto dto) {
        try {
            characterService.selectCharacter(dto);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }


}
