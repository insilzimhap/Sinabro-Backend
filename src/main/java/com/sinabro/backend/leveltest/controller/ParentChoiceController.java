package com.sinabro.backend.leveltest.controller;

import com.sinabro.backend.leveltest.dto.ParentChoiceDTO;
import com.sinabro.backend.leveltest.entity.ParentChoice;
import com.sinabro.backend.leveltest.entity.ParentOption;
import com.sinabro.backend.leveltest.entity.ParentQuestion;
import com.sinabro.backend.leveltest.repository.ParentChoiceRepository;
import com.sinabro.backend.leveltest.repository.ParentOptionRepository;
import com.sinabro.backend.leveltest.repository.ParentQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parent-choice")
@RequiredArgsConstructor
public class ParentChoiceController {

    private final ParentChoiceRepository choiceRepo;
    private final ParentQuestionRepository questionRepo;
    private final ParentOptionRepository optionRepo;

    // 🟤 부모 응답 저장
    @PostMapping("/submit")
    public ResponseEntity<?> submitParentChoices(
            @RequestParam("childId") String childId,
            @RequestBody List<ParentChoiceDTO> choices
    ) {
        for (ParentChoiceDTO dto : choices) {
            ParentQuestion question = questionRepo.findById(dto.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException("문항 ID가 유효하지 않음"));
            ParentOption option = optionRepo.findById(dto.getOptionId())
                    .orElseThrow(() -> new IllegalArgumentException("옵션 ID가 유효하지 않음"));

            ParentChoice choice = new ParentChoice();
            choice.setChildId(childId);
            choice.setQuestion(question);
            choice.setOption(option);

            choiceRepo.save(choice); // DB에 저장!
        }

        return ResponseEntity.ok("부모 응답 저장 완료!");
    }
}
