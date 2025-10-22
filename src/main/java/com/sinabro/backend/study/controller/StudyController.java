package com.sinabro.backend.study.controller;

import com.sinabro.backend.study.dto.StudyCompletionDto;
import com.sinabro.backend.study.service.StudyCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyCompletionService studyCompletionService;

    // 1. '쓰기 학습' 완료 API
    @PostMapping("/writing/complete")
    public ResponseEntity<Void> completeWritingStudy(@RequestBody StudyCompletionDto dto) {
        studyCompletionService.processWritingStudyCompletion(dto);
        return ResponseEntity.ok().build(); // 성공적으로 완료
    }

    // 2. '듣기 학습' 완료 API (신규 추가!)
    @PostMapping("/listening/complete")
    public ResponseEntity<Void> completeListeningStudy(@RequestBody StudyCompletionDto dto) {
        studyCompletionService.processListeningStudyCompletion(dto);
        return ResponseEntity.ok().build(); // 성공적으로 완료
    }
}