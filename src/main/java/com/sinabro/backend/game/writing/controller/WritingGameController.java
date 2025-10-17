package com.sinabro.backend.game.writing.controller;

import com.sinabro.backend.game.writing.dto.*;
import com.sinabro.backend.record.entity.WritingGameResult;
import com.sinabro.backend.game.writing.service.WritingGameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * [앱 > 쓰기 게임 API]
 * - POST   /api/app/games/writing/start        : 쓰기 게임 시작 검증 및 Result 스텁 생성
 * - POST   /api/app/games/writing/choice       : 선택 기록 저장 (문항별)
 * - POST   /api/app/games/writing/complete     : 게임 완료 처리 (결과 계산 및 갱신)
 * - GET    /api/app/games/writing/tree         : 쓰기 나무(열매 진행도) 조회
 *
 * ※ 인증 연동 시 childId는 JWT 주체 기반으로 자동 추출 가능
 */
@RestController
@RequestMapping("/api/app/games/writing")
@RequiredArgsConstructor
@Slf4j
public class WritingGameController {

    private final WritingGameService writingGameService;

    /** 🎬 쓰기 게임 시작 (스텁 생성 + 랜덤 문제 출제) */
    @PostMapping("/start")
    public ResponseEntity<WritingGameStartResponseDto> startWritingGame(
            @RequestBody WritingGameStartRequestDto req
    ) {
        log.info("[WritingGameController][startWritingGame] 호출 childId={} fruitId={}",
                req.getChildId(), req.getFruitId());
        var dto = writingGameService.start(req);
        return ResponseEntity.ok(dto);
    }

    /** ✍️ 선택 기록 저장 (문항별) */
    @PostMapping("/choice")
    public ResponseEntity<Void> saveChoice(
            @RequestBody WritingGameChoiceRequestDto req
    ) {
        log.info("[WritingGameController][saveChoice] 호출 resultId={} questionId={}",
                req.getResultId(), req.getQuestionId());
        writingGameService.choice(req);
        return ResponseEntity.noContent().build(); // 204
    }

    /** ✅ 게임 완료 처리 (결과 집계 및 업데이트) */
    @PostMapping("/complete")
    public ResponseEntity<WritingGameCompleteResponseDto> completeGame(
            @RequestBody WritingGameCompleteRequestDto req
    ) {
        log.info("[WritingGameController][completeGame] 호출 childId={} fruitId={} resultId={}",
                req.getChildId(), req.getFruitId(), req.getResultId());

        // 서비스는 WritingGameResult 엔티티 반환 (DB 업데이트 포함)
        WritingGameResult result = writingGameService.complete(req);

        // 응답 DTO 매핑 (엔티티 직접 반환 방지)
        WritingGameCompleteResponseDto resp = WritingGameCompleteResponseDto.builder()
                .resultId(result.getWgResultId())
                .score(result.getWgScore())
                .success(result.isSuccess())
                .totalQuestions(result.getTotalQuestions())
                .timeSpentSecs(result.getTimeSpentSecs())
                .build();

        return ResponseEntity.ok(resp);
    }

    /** 🌳 나무(열매 진행도) 조회 */
    @GetMapping("/tree")
    public ResponseEntity<?> getWritingGameTree(
            @RequestParam String stageId,
            @RequestParam(required = false) String childId
    ) {
        log.info("[WritingGameController][getWritingGameTree] 호출 stageId={} childId={}", stageId, childId);
        var list = writingGameService.tree(stageId, childId);
        return ResponseEntity.ok(list);
    }
}
