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

@RestController
@RequestMapping("/api/level-test")
@RequiredArgsConstructor
public class LevelTestController {

    private final ParentQuestionRepository parentRepo;
    private final LevelTestQuestionRepository questionRepo;
    private final ChildRepository childRepo;

    @GetMapping("/questions")
    public LevelTestResponseDTO getAllLevelTestData(@RequestParam String childId) {

        // ✅ 1. childId로 유아 정보 불러오기
        Child child = childRepo.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유아를 찾을 수 없습니다."));

        String childName = child.getChildName();  // 또는 childNickName도 가능

        // ✅ 2. 부모 체크리스트
        List<ParentQuestionDTO> parentQuestions = parentRepo.findAll().stream()
                .map(p -> new ParentQuestionDTO(
                        p.getId(),
                        p.getQuestionText(),
                        p.getChoices()
                )).collect(Collectors.toList());

        // ✅ 3. 레벨 테스트 문제
        List<LevelTestQuestionDTO> levelTestQuestions = new ArrayList<>();

        for (LevelTestQuestion q : questionRepo.findAll()) {
            // 👇 이름 고르기 문항이면, 보기 동적으로 구성
            if ("이름 고르기".equals(q.getType())) {
                List<LevelTestOptionDTO> options = new ArrayList<>();

                options.add(new LevelTestOptionDTO(null, childName, null, true)); // 정답

                options.add(new LevelTestOptionDTO(null, "김철수", null, false));
                options.add(new LevelTestOptionDTO(null, "박영희", null, false));
                options.add(new LevelTestOptionDTO(null, "홍길동", null, false));

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
                // 👇 일반 문제는 DB 그대로
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
                        q.getAudioUrl(),
                        q.getQuestionImageUrl(),
                        options
                ));
            }
        }

        return new LevelTestResponseDTO(parentQuestions, levelTestQuestions);
    }
}
