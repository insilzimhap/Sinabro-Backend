package com.sinabro.backend.leveltest.controller;

import com.sinabro.backend.user.child.entity.Child;
import com.sinabro.backend.leveltest.dto.*;
import com.sinabro.backend.leveltest.entity.LevelTestQuestion;
import com.sinabro.backend.leveltest.repository.LevelTestQuestionRepository;
import com.sinabro.backend.leveltest.repository.ParentQuestionRepository;
import com.sinabro.backend.user.child.repository.ChildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

// ── Swagger OpenAPI ────────────────────────────────────────────────────────────
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
// ──────────────────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/api/level-test")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(
        name = "Level Test",
        description = "레벨 테스트(부모 체크리스트 + 유아 문항) 조회 API"
)
public class LevelTestController {

    private final ParentQuestionRepository parentRepo;
    private final LevelTestQuestionRepository questionRepo;
    private final ChildRepository childRepo;

    @GetMapping("/questions")
    @Operation(
            summary = "레벨 테스트 데이터 조회",
            description = """
                    부모 체크리스트 문항들과 유아 레벨 테스트 문항을 함께 반환
                    * 이름 고르기(type="이름 고르기") 문제는 서버가 해당 **자녀 이름**을 정답 텍스트로 동적 치환.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = LevelTestResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청(존재하지 않는 childId 등)",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public LevelTestResponseDTO getAllLevelTestData(
            @Parameter(
                    description = "레벨 테스트를 진행할 **자녀 ID**(PK).\n예) `rami`",
                    required = true,
                    example = "rami"
            )
            @RequestParam("childId") String childId
    ) {

        // ✅ 1. childId로 유아 정보 불러오기
        Child child = childRepo.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유아를 찾을 수 없습니다."));

        String childName = child.getChildName();  // 또는 childNickName도 가능

        // ✅ 2. 부모 체크리스트 (정렬된 순서 + questionOrder 포함)
        List<ParentQuestionDTO> parentQuestions = parentRepo.findAllByOrderByQuestionOrder().stream()
                .map(p -> new ParentQuestionDTO(
                        p.getId(),
                        p.getQuestionOrder(), // 🔥 추가: 정렬/매핑용
                        p.getQuestionText(),
                        p.getOptions().stream()
                                .map(o -> new ParentOptionDTO(o.getId(), o.getOptionText()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        // ✅ 3. 레벨 테스트 문제
        List<LevelTestQuestionDTO> levelTestQuestions = new ArrayList<>();

        for (LevelTestQuestion q : questionRepo.findAll()) {
            if ("이름 고르기".equals(q.getType())) {
                // 👇 DB의 옵션 id는 그대로 유지하면서, 정답 옵션 텍스트만 childName으로 치환
                List<LevelTestOptionDTO> options = q.getOptions().stream()
                        .map(o -> {
                            String text = o.isCorrect() ? childName : o.getOptionText();
                            return new LevelTestOptionDTO(
                                    o.getId(),
                                    text,
                                    o.getImageUrl(),
                                    o.isCorrect()
                            );
                        })
                        .collect(Collectors.toList());

                Collections.shuffle(options);

                levelTestQuestions.add(new LevelTestQuestionDTO(
                        q.getId(),
                        q.getLevel(),
                        q.getType(),
                        q.getPrompt(),
                        q.getQuestionImageUrl(),
                        q.getAudioUrl(),
                        options
                ));
            } else {
                // 👇 일반 문제는 DB 그대로 (❗️순서 주의: questionImageUrl -> audioUrl)
                List<LevelTestOptionDTO> options = q.getOptions().stream()
                        .map(o -> new LevelTestOptionDTO(
                                o.getId(),
                                o.getOptionText(),
                                o.getImageUrl(),
                                o.isCorrect()
                        )).collect(Collectors.toList());

                levelTestQuestions.add(new LevelTestQuestionDTO(
                        q.getId(),
                        q.getLevel(),
                        q.getType(),
                        q.getPrompt(),
                        q.getQuestionImageUrl(),
                        q.getAudioUrl(),
                        options
                ));
            }
        }

        return new LevelTestResponseDTO(parentQuestions, levelTestQuestions);
    }
}
