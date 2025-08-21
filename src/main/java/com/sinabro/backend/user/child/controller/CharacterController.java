package com.sinabro.backend.user.child.controller;

import com.sinabro.backend.user.child.entity.CharacterInfo;
import com.sinabro.backend.user.child.entity.CharacterSelection;
import com.sinabro.backend.user.child.entity.Child;
import com.sinabro.backend.user.child.repository.CharacterSelectionRepository;
import com.sinabro.backend.user.child.repository.ChildRepository;
import com.sinabro.backend.user.child.service.CharacterService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

// ── Swagger OpenAPI ────────────────────────────────────────────────────────────
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// ──────────────────────────────────────────────────────────────────────────────

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api") // 공통 prefix
@Tag(
        name = "Characters",
        description = "캐릭터 카탈로그 조회 및 선택 저장 API"
)
public class CharacterController {

    private final CharacterService characterService;                 // 목록/이름→ID
    private final CharacterSelectionRepository selectionRepository;  // 선택 저장
    private final ChildRepository childRepository;                   // child 존재 확인

    // === [카탈로그 - 조회] ===

    @GetMapping("/characters")
    @Operation(
            summary = "캐릭터 목록 조회",
            description = """
DB의 characters 테이블에서 모든 캐릭터 정보를 조회.
characterId, characterName, imageUrl을 반환.
"""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "캐릭터 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = CharacterDto.class))
            )
    })
    public ResponseEntity<List<CharacterDto>> list() {
        List<CharacterInfo> list = characterService.listAll();
        return ResponseEntity.ok(
                list.stream()
                        .map(c -> new CharacterDto(c.getCharacterId(), c.getCharacterName(), c.getImageUrl()))
                        .toList()
        );
    }

    @GetMapping("/characters/resolve")
    @Operation(
            summary = "캐릭터 이름으로 ID 조회",
            description = """
캐릭터 이름을 기준으로 characterId를 조회.
"""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "캐릭터 ID 조회 성공",
                    content = @Content(schema = @Schema(implementation = CharacterDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "존재하지 않는 캐릭터 이름",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<CharacterDto> resolve(
            @Parameter(
                    description = "캐릭터 이름",
                    required = true,
                    example = "토끔"
            )
            @RequestParam("name") String name
    ) {
        String id = characterService.resolveIdByName(name);
        return ResponseEntity.ok(new CharacterDto(id, name, null));
    }

    // === [선택 저장 - 쓰기] ===

    @PostMapping("/character/selection")
    @Operation(
            summary = "캐릭터 선택 저장",
            description = """
자녀(childId)가 특정 캐릭터(characterId)를 선택하면 character_selection 테이블에 저장. 
이미 선택한 경우 409 반환.  
"""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "캐릭터 선택 저장 성공",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "childId 또는 characterId 누락/유효하지 않음",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 선택된 캐릭터 존재",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<?> saveSelection(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "자녀 ID와 선택한 캐릭터 ID",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CharacterSelectReq.class))
            )
            @RequestBody CharacterSelectReq req
    ) {
        if (req.getChildId() == null || req.getChildId().isBlank()
                || req.getCharacterId() == null || req.getCharacterId().isBlank()) {
            return ResponseEntity.badRequest().body("childId/characterId가 필요합니다.");
        }

        Optional<Child> childOpt = childRepository.findById(req.getChildId());
        if (childOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 childId: " + req.getChildId());
        }

        if (selectionRepository.findByChildId(req.getChildId()).isPresent()) {
            return ResponseEntity.status(409).body("이미 캐릭터를 선택했습니다.");
        }

        CharacterSelection cs = new CharacterSelection();
        cs.setChildId(req.getChildId());
        cs.setCharacterId(req.getCharacterId());
        selectionRepository.save(cs);

        Map<String, Object> res = new HashMap<>();
        res.put("message", "캐릭터 선택이 저장되었습니다.");
        return ResponseEntity.ok(res);
    }

    // === DTOs ===
    @Data
    @AllArgsConstructor
    static class CharacterDto {
        @Schema(description = "캐릭터 ID", example = "C001")
        private String characterId;

        @Schema(description = "캐릭터 이름", example = "토끔")
        private String characterName;

        @Schema(description = "캐릭터 이미지 URL", example = "https://cdn.example.com/characters/tokkeum.png")
        private String imageUrl;
    }

    @Data
    static class CharacterSelectReq {
        @Schema(description = "자녀 ID (PK)", example = "child001")
        private String childId;

        @Schema(description = "선택한 캐릭터 ID", example = "C001")
        private String characterId;
    }
}
