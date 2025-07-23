package com.sinabro.backend.leveltest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class LevelTestResponseDTO {
    private List<ParentQuestionDTO> parentQuestions;
    private List<LevelTestQuestionDTO> levelTestQuestions;
}
