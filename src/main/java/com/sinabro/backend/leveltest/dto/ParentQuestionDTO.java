package com.sinabro.backend.leveltest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ParentQuestionDTO {
    private Long id;
    private String questionText;
    private List<ParentOptionDTO> options;
}
