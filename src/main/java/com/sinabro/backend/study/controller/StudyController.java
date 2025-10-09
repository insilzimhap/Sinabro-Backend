package com.sinabro.backend.study.controller;//package com.sinabro.backend.study.controller;
//
//import com.sinabro.backend.stage.entity.Stage;
//import com.sinabro.backend.stage.repository.StageRepository;
//import com.sinabro.backend.study.entity.StudyListeningContent;
//import com.sinabro.backend.study.entity.StudyWritingContent;
//import com.sinabro.backend.study.repository.StudyListeningContentRepository;
//import com.sinabro.backend.study.repository.StudyWritingContentRepository;
//import com.sinabro.backend.record.entity.ListeningRecord;
//import com.sinabro.backend.record.entity.WritingRecord;
//import com.sinabro.backend.record.repository.ListeningRecordRepository;
//import com.sinabro.backend.record.repository.WritingRecordRepository;
//import com.sinabro.backend.progress.entity.Progress;
//import com.sinabro.backend.progress.repository.ProgressRepository;
//import com.sinabro.backend.user.entity.Child;
//import com.sinabro.backend.user.repository.ChildRepository;
//
//// ⬇⬇⬇ DTO 임포트는 'StudyDtos'의 중첩 타입을 임포트!
//import com.sinabro.backend.study.dto.StudyDtos.*;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalTime;           // ⬅ 시간 파싱용
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/study")
//@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
//@Tag(name = "Study", description = "듣기/쓰기 학습 조회 & 기록 (채점 없음)")
//public class StudyController {
//
//    private final StageRepository stageRepo;
//    private final StudyListeningContentRepository lsRepo;
//    private final StudyWritingContentRepository wsRepo;
//    private final ListeningRecordRepository lsRecRepo;
//    private final WritingRecordRepository wsRecRepo;
//    private final ProgressRepository progressRepo;
//    private final ChildRepository childRepo;
//
//    // ─────────────────────────────────────────────────────────────
//    // Stage 조회
//    // ─────────────────────────────────────────────────────────────
//    @GetMapping("/stages")
//    @Operation(summary = "스테이지 목록 조회",
//            description = "category 필수, level 선택. stageId 오름차순 정렬.")
//    public List<StageDTO> getStages(
//            @RequestParam("category") String category,
//            @RequestParam(value = "level", required = false) String level
//    ) {
//        List<Stage> stages = (level == null || level.isBlank())
//                ? stageRepo.findByCategoryOrderByStageIdAsc(category)
//                : stageRepo.findByCategoryAndLevelOrderByStageIdAsc(category, level);
//
//        return stages.stream().map(s ->
//                StageDTO.builder()
//                        .stageId(s.getStageId())
//                        .category(s.getCategory())
//                        .level(s.getLevel())
//                        .build()
//        ).collect(Collectors.toList());
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // 듣기 컨텐츠 조회 (단계별)
//    // ─────────────────────────────────────────────────────────────
//    @GetMapping("/listening/contents")
//    @Operation(summary = "듣기 학습 컨텐츠 조회", description = "단계 내 순서(ls_content_order)로 정렬.")
//    public List<ListeningContentDTO> getListeningContents(
//            @RequestParam("stageId") String stageId
//    ) {
//        return lsRepo.findByStage_StageIdOrderByLsContentOrderAsc(stageId)
//                .stream().map(c ->
//                        ListeningContentDTO.builder()
//                                .contentId(c.getLsContentId())
//                                .contentType(c.getLsContentType())
//                                .subjectTag(c.getLsSubjectTag())
//                                .imageUrl(c.getLsImageUrl())
//                                .audioUrl(c.getLsAudioUrl())
//                                .order(c.getLsContentOrder())
//                                .build()
//                ).collect(Collectors.toList());
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // 쓰기 컨텐츠 조회 (단계별)
//    // ─────────────────────────────────────────────────────────────
//    @GetMapping("/writing/contents")
//    @Operation(summary = "쓰기 학습 컨텐츠 조회", description = "단계 내 순서(ws_content_order)로 정렬.")
//    public List<WritingContentDTO> getWritingContents(
//            @RequestParam("stageId") String stageId
//    ) {
//        return wsRepo.findByStage_StageIdOrderByWsContentOrderAsc(stageId)
//                .stream().map(c ->
//                        WritingContentDTO.builder()
//                                .contentId(c.getWsContentId())
//                                .subjectTag(c.getWsSubjectTag())
//                                .contentType(c.getWsContentType())
//                                .contentText(c.getWsContentText())
//                                .imageUrl(c.getWsImageUrl())
//                                .audioUrl(c.getWsAudioUrl())
//                                .strokeImageUrl(c.getWsStrokeImageUrl())
//                                .order(c.getWsContentOrder())
//                                .build()
//                ).collect(Collectors.toList());
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // 듣기 학습 기록 저장 + 진행도 갱신
//    // ─────────────────────────────────────────────────────────────
//    @PostMapping("/listening/record")
//    @Transactional
//    @Operation(summary = "듣기 학습 기록 저장",
//            description = "완료여부/소요시간만 저장. Progress.last_ls_stage_id 업데이트, completed=true면 best_ls_stage_id 갱신.")
//    public SimpleMessage saveListeningRecord(@RequestBody ListeningRecordRequest req) {
//
//        Child child = childRepo.findById(req.getChildId())
//                .orElseThrow(() -> new IllegalArgumentException("childId가 존재하지 않음"));
//
//        StudyListeningContent content = lsRepo.findById(req.getContentId())
//                .orElseThrow(() -> new IllegalArgumentException("contentId가 존재하지 않음"));
//
//        if (!content.getStage().getStageId().equals(req.getStageId())) {
//            throw new IllegalArgumentException("content가 해당 stageId에 속하지 않습니다.");
//        }
//
//        // time 문자열(HH:mm:ss) → LocalTime (엔티티가 TIME이면)
//        LocalTime time = null;
//        if (req.getTimeSpent() != null && !req.getTimeSpent().isBlank()) {
//            time = LocalTime.parse(req.getTimeSpent());
//        }
//
//        boolean completed = Boolean.TRUE.equals(req.getCompleted());
//
//        ListeningRecord rec = ListeningRecord.builder()
//                .lsRecordId(UUID.randomUUID().toString())
//                .resultType("듣기 학습")
//                .lsTimeSpent(time)              // ← 엔티티가 String이면 .toString() 쓰거나 타입 맞춰줘
//                .lsCompleted(completed)
//                .listeningContent(content)
//                .child(child)
//                .build();
//        lsRecRepo.save(rec);
//
//        Progress p = progressRepo.findById(child.getChildId())
//                .orElse(Progress.builder().childId(child.getChildId()).build());
//        p.setLastLsStageId(req.getStageId());
//        if (completed) p.setBestLsStageId(req.getStageId());
//        progressRepo.save(p);
//
//        return SimpleMessage.builder().message("듣기 학습 기록 저장 완료").build();
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // 쓰기 학습 기록 저장 + 진행도 갱신
//    // ─────────────────────────────────────────────────────────────
//    @PostMapping("/writing/record")
//    @Transactional
//    @Operation(summary = "쓰기 학습 기록 저장",
//            description = "완료여부/소요시간만 저장. Progress.last_ws_stage_id 업데이트, completed=true면 best_ws_stage_id 갱신.")
//    public SimpleMessage saveWritingRecord(@RequestBody WritingRecordRequest req) {
//
//        Child child = childRepo.findById(req.getChildId())
//                .orElseThrow(() -> new IllegalArgumentException("childId가 존재하지 않음"));
//
//        StudyWritingContent content = wsRepo.findById(req.getContentId())
//                .orElseThrow(() -> new IllegalArgumentException("contentId가 존재하지 않음"));
//
//        if (!content.getStage().getStageId().equals(req.getStageId())) {
//            throw new IllegalArgumentException("content가 해당 stageId에 속하지 않습니다.");
//        }
//
//        boolean completed = Boolean.TRUE.equals(req.getCompleted());
//
//        WritingRecord rec = WritingRecord.builder()
//                .wsRecordId(UUID.randomUUID().toString())
//                .resultType("쓰기 학습")
//                .wsTimeSpent(req.getTimeSpentMinutes())   // 엔티티가 INT(분)
//                .wsCompleted(completed)
//                .writingContent(content)
//                .child(child)
//                .build();
//        wsRecRepo.save(rec);
//
//        Progress p = progressRepo.findById(child.getChildId())
//                .orElse(Progress.builder().childId(child.getChildId()).build());
//        p.setLastWsStageId(req.getStageId());
//        if (completed) p.setBestWsStageId(req.getStageId());
//        progressRepo.save(p);
//
//        return SimpleMessage.builder().message("쓰기 학습 기록 저장 완료").build();
//    }
//}
