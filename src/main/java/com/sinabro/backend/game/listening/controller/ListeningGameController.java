package com.sinabro.backend.game.listening.controller;

import com.sinabro.backend.game.listening.dto.*;
import com.sinabro.backend.record.entity.ListeningGameResult;
import com.sinabro.backend.game.listening.service.ListeningGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * [앱 > 듣기 게임 API]
 * - POST   /api/app/games/listening/start        : 듣기 게임 시작 검증 및 Result 스텁 생성
 * - POST   /api/app/games/listening/choice       : 선택 기록 저장 (문항별)
 * - POST   /api/app/games/listening/complete     : 게임 완료 처리 (결과 계산 및 갱신)
 * - GET    /api/app/games/listening/tree         : 듣기 나무(열매 진행도) 조회
 * - POST   /api/app/games/listening/report       : [리포트용] 결과 수집 및 분석 (AI 리포트용)
 *
 * ※ 인증 연동 시 childId는 JWT 주체 기반으로 자동 추출 가능
 */
@RestController
@RequestMapping("/api/app/games/listening")
@RequiredArgsConstructor
@Slf4j
public class ListeningGameController {

    private final ListeningGameService listeningGameService;


    /** 🎬 듣기 게임 시작 (스텁 생성) */
    @PostMapping("/start") //changed
    public ResponseEntity<ListeningGameStartResponseDto> startListeningGame(
            @RequestBody ListeningGameStartRequestDto req
    ) {
        log.info("[ListeningGameController][startListeningGame] 호출 childId={} fruitId={}", req.getChildId(), req.getFruitId());
        var dto = listeningGameService.start(req);
        return ResponseEntity.ok(dto);
    }

    /** 📝 선택 기록 저장 (문항별) */
    @PostMapping("/choice") //changed
    public ResponseEntity<Void> saveChoice(
            @RequestBody ListeningGameChoiceRequestDto req
    ) {
        log.info("[ListeningGameController][saveChoice] 호출 resultId={} questionId={} optionId={}",
                req.getResultId(), req.getQuestionId(), req.getOptionId());
        listeningGameService.choice(req);
        return ResponseEntity.noContent().build(); // 204
    }

    /** ✅ 게임 완료 처리 (결과 집계 및 업데이트) */
    @PostMapping("/complete") //changed
    public ResponseEntity<ListeningGameCompleteResponseDto> completeGame(
            @RequestBody ListeningGameCompleteRequestDto req
    ) {
        log.info("[ListeningGameController][completeGame] 호출 childId={} fruitId={} resultId={}",
                req.getChildId(), req.getFruitId(), req.getResultId());

        // 서비스는 기존대로 ListeningGameResult 리턴 (DB 업데이트 포함)
        ListeningGameResult result = listeningGameService.complete(req); // unchanged: 서비스 시그니처 그대로

        // 엔티티를 응답 DTO로 맵핑 (순환참조/Jackson 문제 회피)
        ListeningGameCompleteResponseDto resp = ListeningGameCompleteResponseDto.builder()
                .resultId(result.getLgResultId())
                .score(result.getLgScore())
                .success(result.isSuccess())
                .totalQuestions(result.getTotalQuestions())
                .timeSpentSecs(result.getTimeSpentSecs())
                .build();

        return ResponseEntity.ok(resp); //changed
    }

    /** 🌳 나무(열매 진행도) 조회 */
    @GetMapping("/tree") //changed
    public ResponseEntity<?> getListeningGameTree(
            @RequestParam String stageId,
            @RequestParam(required = false) String childId
    ) {
        log.info("[ListeningGameController][getListeningGameTree] 호출 stageId={} childId={}", stageId, childId);
        var list = listeningGameService.tree(stageId, childId);
        return ResponseEntity.ok(list);
    }

    /** 📊 [리포트용] 결과 수집 및 분석 (AI 학습 리포트에 사용) */
    @PostMapping("/report") //changed
    public ResponseEntity<String> processListeningGameResult(
            @RequestBody ListeningGameResultDto resultDto
    ) {
        log.info("[ListeningGameController][processListeningGameResult] 호출 childId={} fruitId={}",
                resultDto.getChildId(), resultDto.getFruitId());
        listeningGameService.processListeningGameResult(resultDto);
        return ResponseEntity.ok("Listening game result processed successfully.");
    }
}