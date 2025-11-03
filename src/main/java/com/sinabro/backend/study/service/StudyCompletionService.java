package com.sinabro.backend.study.service;

import com.sinabro.backend.record.entity.ListeningRecord;
import com.sinabro.backend.record.entity.WritingRecord;
import com.sinabro.backend.record.repository.ListeningRecordRepository;
import com.sinabro.backend.record.repository.WritingRecordRepository;
import com.sinabro.backend.reward.dto.RewardStickerRequestDto;
import com.sinabro.backend.reward.service.RewardService;
import com.sinabro.backend.progress.repository.ChildProgressRepository; // ⭐️ Import 추가!
import com.sinabro.backend.progress.entity.ChildProgress;           // ⭐️ Entity Import 추가!
import com.sinabro.backend.progress.entity.ChildProgressId;         // ⭐️ ID Class Import 추가!
import com.sinabro.backend.progress.entity.Category;


// Stage 관련 Import
import com.sinabro.backend.stage.entity.ChildFruitStatus;
import com.sinabro.backend.stage.entity.ChildFruitStatusId;
import com.sinabro.backend.stage.entity.LearningFruit;
import com.sinabro.backend.stage.entity.Stage; // ⭐️ Stage Import
import com.sinabro.backend.stage.repository.ChildFruitStatusRepository;
import com.sinabro.backend.stage.repository.LearningFruitRepository;
import com.sinabro.backend.stage.repository.StageRepository; // ⭐️ StageRepository Import

import com.sinabro.backend.study.dto.StudyCompletionDto;
import com.sinabro.backend.study.entity.StudyWritingContent;
import com.sinabro.backend.study.entity.StudyListeningContent;
import com.sinabro.backend.study.repository.StudyListeningContentRepository;
import com.sinabro.backend.study.repository.StudyWritingContentRepository;

// User 관련 Import
import com.sinabro.backend.user.entity.Child;
import com.sinabro.backend.user.repository.ChildRepository;
import com.sinabro.backend.weakness.service.ChildWeaknessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime; // ⭐️ LocalDateTime Import
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyCompletionService {

    // Stage Repositories
    private final LearningFruitRepository learningFruitRepository;
    private final ChildFruitStatusRepository childFruitStatusRepository;
    private final StageRepository stageRepository; // ⭐️ StageRepository 주입
    private final StudyListeningContentRepository studyListeningContentRepository;


    //progress Repositories
    private final ChildProgressRepository childProgressRepository;

    // Other Repositories & Services
    private final StudyWritingContentRepository studyWritingContentRepository;
    private final ListeningRecordRepository listeningRecordRepository;
    private final WritingRecordRepository writingRecordRepository;
    private final ChildRepository childRepository;
    private final ChildWeaknessService childWeaknessService;
    private final RewardService rewardService;

    // --- '쓰기 학습' 처리 메서드 ---
    @Transactional
    public void processWritingStudyCompletion(StudyCompletionDto dto) {
        log.info("[StudyCompletion] '쓰기 학습' 완료 처리 시작: childId={}, fruitId={}", dto.getChildId(), dto.getFruitId());

        List<StudyWritingContent> contents = studyWritingContentRepository.findByFruit_FruitIdOrderByContentOrderAsc(dto.getFruitId());
        if (contents.isEmpty()) {
            // 콘텐츠가 없는 경우 예외 처리 또는 로그 기록 (여기서는 예외 발생)
            log.error("[StudyCompletion] fruitId '{}'에 해당하는 쓰기 콘텐츠를 찾을 수 없습니다.", dto.getFruitId());
            throw new RuntimeException("Error: Content not found for fruitId " + dto.getFruitId());
        }
        String contentId = contents.get(0).getWsContentId();

        WritingRecord record = WritingRecord.builder()
                .wsRecordId("ws-rec-" + UUID.randomUUID())
                .wsChildId(dto.getChildId())
                .fruitId(dto.getFruitId())
                .wsContentId(contentId)
                .wsCompleted(dto.isCompleted())
                .timeSpentSecs(dto.getTimeSpentSecs())
                .resultType("쓰기 학습")
                .build();
        writingRecordRepository.save(record);
        log.info("[StudyCompletion] 쓰기 학습 기록 저장 완료: recordId={}", record.getWsRecordId());

        // ⭐️ ChildProgress 업데이트 호출! (Category.writing_study 사용)
        updateChildProgress(dto.getChildId(), Category.writing_study, dto.getFruitId());

        Child child = childRepository.findById(dto.getChildId())
                .orElseThrow(() -> new RuntimeException("Child not found: " + dto.getChildId()));

        childWeaknessService.analyzeAndUpsertWeakness(child, dto.getFruitId());

        if (dto.isCompleted()) {
            log.info("[StudyCompletion] 학습 완료됨 (isCompleted=true). 보상 지급 및 다음 열매/스테이지 활성화 로직 실행.");
            RewardStickerRequestDto stickerRequest = new RewardStickerRequestDto(
                    dto.getChildId(), dto.getFruitId()
            );
            rewardService.giveSticker(stickerRequest);

            // 다음 열매 또는 다음 스테이지 활성화 함수 호출!
            activateNextStudyFruit(dto.getChildId(), dto.getFruitId());
        } else {
            log.info("[StudyCompletion] 학습 미완료 (isCompleted=false). 보상/활성화 건너뜀.");
        }
        log.info("[StudyCompletion] '쓰기 학습' 완료 처리 종료: childId={}, fruitId={}", dto.getChildId(), dto.getFruitId());
    }

    // --- '듣기 학습' 처리 메서드 ---
    @Transactional
    public void processListeningStudyCompletion(StudyCompletionDto dto) {
        log.info("[StudyCompletion] '듣기 학습' 완료 처리 시작: childId={}, fruitId={}", dto.getChildId(), dto.getFruitId());

        // ⭐️ [추가] 듣기 콘텐츠 ID 조회 (필수)
        //    (DB 스키마에 ls_content_id가 있으므로 '쓰기'처럼 조회해야 함)
        List<StudyListeningContent> contents = studyListeningContentRepository.findByFruit_FruitIdOrderByContentOrderAsc(dto.getFruitId());
        if (contents.isEmpty()) {
            // 콘텐츠가 없는 경우 예외 처리 또는 로그 기록
            log.error("[StudyCompletion] fruitId '{}'에 해당하는 듣기 콘텐츠를 찾을 수 없습니다.", dto.getFruitId());
            throw new RuntimeException("Error: Content not found for fruitId " + dto.getFruitId());
        }
        // fruitId 하나에 여러 콘텐츠가 있어도, '쓰기'와 동일하게 첫 번째 콘텐츠 ID를 저장
        String contentId = contents.get(0).getLsContentId();
        log.debug("[StudyCompletion] 듣기 콘텐츠 ID 조회 완료: lsContentId={}", contentId);

        ListeningRecord record = ListeningRecord.builder()
                .lsRecordId("ls-rec-" + UUID.randomUUID())
                .lsChildId(dto.getChildId())
                .fruitId(dto.getFruitId())
                .lsContentId(contentId) // ⭐️ [추가] 조회한 contentId 저장
                .lsCompleted(dto.isCompleted())
                .timeSpentSecs(dto.getTimeSpentSecs())
                .resultType("듣기 학습")
                .build();
        listeningRecordRepository.save(record);
        log.info("[StudyCompletion] 듣기 학습 기록 저장 완료: recordId={}", record.getLsRecordId());

        // ⭐️ ChildProgress 업데이트 호출! (Category.listening_study 사용)
        updateChildProgress(dto.getChildId(), Category.listening_study, dto.getFruitId());

        Child child = childRepository.findById(dto.getChildId())
                .orElseThrow(() -> new RuntimeException("Child not found: " + dto.getChildId()));

//        // ⭐️ 취약점 분석 호출 (이건 이미 ls_content_id 조회 로직이 있었음)
//        childWeaknessService.analyzeAndUpsertWeakness(child, dto.getFruitId());

        if (dto.isCompleted()) {
            log.info("[StudyCompletion] 학습 완료됨 (isCompleted=true). 보상 지급 및 다음 열매/스테이지 활성화 로직 실행.");
            RewardStickerRequestDto stickerRequest = new RewardStickerRequestDto(
                    dto.getChildId(), dto.getFruitId()
            );
            rewardService.giveSticker(stickerRequest);

            // 다음 열매 또는 다음 스테이지 활성화 함수 호출!
            activateNextStudyFruit(dto.getChildId(), dto.getFruitId());
        } else {
            log.info("[StudyCompletion] 학습 미완료 (isCompleted=false). 보상/활성화 건너뜀.");
        }
        log.info("[StudyCompletion] '듣기 학습' 완료 처리 종료: childId={}, fruitId={}", dto.getChildId(), dto.getFruitId());
    }

    // ⭐️ 다음 학습 열매 또는 다음 스테이지 첫 열매를 활성화하는 메서드
    private void activateNextStudyFruit(String childId, String completedFruitId) {
        log.debug("[StudyCompletion] 다음 열매/스테이지 활성화 로직 시작: childId={}, completedFruitId={}", childId, completedFruitId);

        LearningFruit completedFruit = learningFruitRepository.findById(completedFruitId)
                .orElseThrow(() -> {
                    log.error("[StudyCompletion] 활성화 실패: 완료된 열매 정보 없음 (fruitId={})", completedFruitId);
                    return new RuntimeException("Fruit not found: " + completedFruitId);
                });

        String stageId = completedFruit.getStageId();
        int currentSequence = completedFruit.getSequenceInStage();
        log.debug("[StudyCompletion] 완료된 열매 정보: stageId={}, sequence={}", stageId, currentSequence);

        // 같은 스테이지의 다음 순서 열매 조회
        Optional<LearningFruit> nextFruitOpt = learningFruitRepository
                .findByStageIdAndSequenceInStage(stageId, currentSequence + 1);

        if (nextFruitOpt.isPresent()) {
            // 다음 열매가 있으면 활성화
            LearningFruit nextFruit = nextFruitOpt.get();
            activateSpecificFruit(childId, nextFruit); // ⭐️ 활성화 로직 분리

        } else {
            // 다음 열매가 없으면? (스테이지 완료) -> 레벨업 로직 실행
            log.info("[StudyCompletion] 스테이지 완료 (다음 열매 없음): childId={}, stageId={}", childId, stageId);
            // ⭐️ 완료된 Stage 엔티티 정보가 필요하므로 다시 조회 (completedFruit에서 가져와도 되지만, 명확성을 위해)
            Stage completedStage = stageRepository.findById(stageId)
                    .orElseThrow(() -> {
                        log.error("[StudyCompletion] 레벨업 실패: 완료된 스테이지 정보 없음 (stageId={})", stageId);
                        return new RuntimeException("Stage not found: " + stageId);
                    });
            handleStageCompletionAndLevelUp(childId, completedStage); // ⭐️ 레벨업 함수 호출!
        }
    }

    // ⭐️ (신규 추가!) 특정 열매를 활성화하는 메서드 (중복 로직 분리)
    private void activateSpecificFruit(String childId, LearningFruit fruitToActivate) {
        String fruitId = fruitToActivate.getFruitId();
        log.debug("[StudyCompletion] 특정 열매 활성화 시도: childId={}, fruitId={}", childId, fruitId);

        ChildFruitStatus status = childFruitStatusRepository
                .findByChildIdAndFruitId(childId, fruitId)
                .orElseGet(() -> { // 없으면 새로 만들기 (Builder 사용)
                    log.debug("[StudyCompletion] ChildFruitStatus 없음. 새로 생성: childId={}, fruitId={}", childId, fruitId);
                    Child childRef = childRepository.getReferenceById(childId);
                    // LearningFruit fruitRef = learningFruitRepository.getReferenceById(fruitId); // 이미 fruitToActivate 객체가 있음
                    return ChildFruitStatus.builder()
                            .id(ChildFruitStatusId.builder().childId(childId).fruitId(fruitId).build())
                            .child(childRef)
                            .learningFruit(fruitToActivate) // 조회한 객체 사용
                            .isActive(false)
                            .build();
                });

        if (!status.isActive()) {
            status.setActive(true); // ⭐️ Setter 호출! (ChildFruitStatus에 @Setter 필요)
            // openedAt은 @CreationTimestamp에 의해 자동 관리됨
            childFruitStatusRepository.save(status);
            log.info("[StudyCompletion] 열매 활성화 완료: childId={}, fruitId={}", childId, fruitId);
        } else {
            log.warn("[StudyCompletion] 열매가 이미 활성화 상태입니다: childId={}, fruitId={}", childId, fruitId);
        }
    }


    // ⭐️ (구현 완료!) 스테이지 완료 시 레벨업 및 다음 스테이지 첫 열매 활성화
    private void handleStageCompletionAndLevelUp(String childId, Stage completedStage) {
        log.info("[StudyCompletion] 스테이지 완료 및 레벨업 처리 시작: childId={}, completedStageId={}, currentLevel={}",
                childId, completedStage.getStageId(), completedStage.getLevel());

        String currentLevelStr = completedStage.getLevel(); // "초급", "중급", "고급"
        Stage.Category category = completedStage.getCategory(); // Enum 타입
        String nextLevelStr = null;
        Integer nextLevelInt = null;

        // 1. 다음 레벨 결정 (문자열 & 숫자)
        if ("초급".equals(currentLevelStr)) {
            nextLevelStr = "중급";
            nextLevelInt = 2;
        } else if ("중급".equals(currentLevelStr)) {
            nextLevelStr = "고급";
            nextLevelInt = 3;
        } else if ("고급".equals(currentLevelStr)) {
            log.info("[StudyCompletion] 최고 레벨(고급) 스테이지 완료. 레벨업 및 다음 스테이지 활성화 없음.");
            return; // 고급 레벨 완료 시 프로세스 종료
        } else {
            log.warn("[StudyCompletion] 알 수 없는 레벨 문자열입니다: {}. 레벨업 처리 중단.", currentLevelStr);
            return;
        }
        log.debug("[StudyCompletion] 다음 레벨 결정: {}", nextLevelStr);

        // 2. 다음 레벨의 첫 번째 스테이지 찾기
        //    findByCategoryAndLevelOrderByStageIdAsc 사용 (반환 타입: List<Stage>)
        List<Stage> nextLevelStages = stageRepository
                .findByCategoryAndLevelOrderByStageIdAsc(category.name(), nextLevelStr); // ⚠️ Enum.name()으로 String 변환

        if (!nextLevelStages.isEmpty()) {
            Stage nextStage = nextLevelStages.get(0); // 첫 번째 스테이지 선택
            log.info("[StudyCompletion] 다음 레벨({})의 첫 스테이지 찾음: nextStageId={}", nextLevelStr, nextStage.getStageId());

            // 3. 다음 스테이지의 첫 번째 열매(sequence=1) 찾기
            Optional<LearningFruit> firstFruitOpt = learningFruitRepository
                    .findByStageIdAndSequenceInStage(nextStage.getStageId(), 1);

            if (firstFruitOpt.isPresent()) {
                LearningFruit firstFruit = firstFruitOpt.get();
                log.info("[StudyCompletion] 다음 스테이지 첫 열매 찾음: firstFruitId={}", firstFruit.getFruitId());

                // 4. 다음 스테이지 첫 열매 활성화 (분리된 메서드 사용)
                activateSpecificFruit(childId, firstFruit);

                // 5. 자녀 레벨 업데이트 (Child 엔티티 childLevel 필드는 Integer 타입)
                Child child = childRepository.findById(childId)
                        .orElseThrow(() -> {
                            log.error("[StudyCompletion] 레벨업 실패: 자녀 정보 없음 (childId={})", childId);
                            return new RuntimeException("Child not found: " + childId);
                        });
                child.setChildLevel(nextLevelInt); // ⭐️ 숫자 레벨로 업데이트! (Child 엔티티에 @Setter 필요)
                childRepository.save(child);
                log.info("[StudyCompletion] 자녀 레벨 업데이트 완료: childId={}, newLevelInt={}, newLevelStr={}", childId, nextLevelInt, nextLevelStr);

            } else {
                log.warn("[StudyCompletion] 다음 스테이지({})에 첫 번째 열매(sequence=1)가 없습니다. 활성화/레벨업 중단.", nextStage.getStageId());
            }
        } else {
            log.warn("[StudyCompletion] 다음 레벨({})에 해당하는 스테이지를 찾을 수 없습니다. 활성화/레벨업 중단.", nextLevelStr);
        }
    }

    /**
     * 학습/게임 완료 시 ChildProgress 테이블을 업데이트하는 메서드
     * @param childId 완료한 자녀 ID
     * @param category 완료한 카테고리 (Category Enum 타입)
     * @param completedFruitId 완료한 열매 ID
     */
    private void updateChildProgress(String childId, Category category, String completedFruitId) {
        log.debug("[StudyCompletion] ChildProgress 업데이트 시작: childId={}, category={}, completedFruitId={}",
                childId, category, completedFruitId);

        // 1. 완료된 열매 정보 조회 (비교를 위해 필요)
        LearningFruit completedFruit = learningFruitRepository.findById(completedFruitId)
                .orElseThrow(() -> {
                    log.error("[StudyCompletion] ChildProgress 업데이트 실패: 완료된 열매 정보 없음 (fruitId={})", completedFruitId);
                    return new RuntimeException("Fruit not found: " + completedFruitId);
                });

        // 2. 기존 ChildProgress 정보 조회 또는 새로 생성
        ChildProgressId progressId = new ChildProgressId(childId, category); // 복합키 생성
        ChildProgress progress = childProgressRepository.findById(progressId)
                .orElseGet(() -> { // 없으면 새로 만들기
                    log.debug("[StudyCompletion] ChildProgress 없음. 새로 생성: childId={}, category={}", childId, category);
                    Child childRef = childRepository.getReferenceById(childId);
                    // LearningFruit lastFruitRef = learningFruitRepository.getReferenceById(completedFruitId); // 아래에서 설정
                    // LearningFruit bestFruitRef = learningFruitRepository.getReferenceById(completedFruitId); // 아래에서 설정
                    return ChildProgress.builder()
                            .childId(childId)     // ID 클래스 필드 직접 설정
                            .category(category) // ID 클래스 필드 직접 설정
                            .child(childRef)
                            // lastLearningFruit, bestLearningFruit는 ID 설정 후 JPA가 관리
                            .build();
                });

        // 3. last_fruit_id 업데이트
        progress.setLastFruitId(completedFruitId);
        // progress.setLastUpdatedAt(Timestamp.from(Instant.now())); // @UpdateTimestamp가 관리

        // 4. best_fruit_id 업데이트 (필요 시)
        String currentBestFruitId = progress.getBestFruitId();
        boolean updateBest = false;

        if (currentBestFruitId == null) {
            // 기존 최고 기록이 없으면 무조건 업데이트
            updateBest = true;
            log.debug("[StudyCompletion] 기존 BestFruit 없음. 업데이트 필요.");
        } else {
            // 기존 최고 기록 열매 정보 조회
            LearningFruit currentBestFruit = learningFruitRepository.findById(currentBestFruitId)
                    .orElse(null); // 최고 기록 열매가 삭제되었을 수도 있음

            if (currentBestFruit == null) {
                // 기존 최고 열매 정보가 없으면 (데이터 오류 등) 업데이트
                updateBest = true;
                log.warn("[StudyCompletion] 기존 BestFruit ID({})에 해당하는 열매 정보 없음. 업데이트 필요.", currentBestFruitId);
            } else {
                // 완료된 열매와 기존 최고 열매 비교
                // 비교 기준: stageId (문자열 비교) -> sequenceInStage (숫자 비교)
                int stageCompare = completedFruit.getStageId().compareTo(currentBestFruit.getStageId());
                if (stageCompare > 0) {
                    // 완료된 열매의 스테이지가 더 높으면 업데이트
                    updateBest = true;
                } else if (stageCompare == 0 && completedFruit.getSequenceInStage() > currentBestFruit.getSequenceInStage()) {
                    // 같은 스테이지 내에서 완료된 열매의 순서가 더 높으면 업데이트
                    updateBest = true;
                }
                log.debug("[StudyCompletion] BestFruit 비교: completed(stage={}, seq={}) vs currentBest(stage={}, seq={}). UpdateNeeded={}",
                        completedFruit.getStageId(), completedFruit.getSequenceInStage(),
                        currentBestFruit.getStageId(), currentBestFruit.getSequenceInStage(), updateBest);
            }
        }

        if (updateBest) {
            progress.setBestFruitId(completedFruitId);
            // progress.setBestUpdatedAt(Timestamp.from(Instant.now())); // 필요 시 직접 업데이트
            log.info("[StudyCompletion] BestFruit 업데이트: childId={}, category={}, newBestFruitId={}",
                    childId, category, completedFruitId);
        }

        // 5. ChildProgress 저장 (INSERT or UPDATE)
        childProgressRepository.save(progress);
        log.info("[StudyCompletion] ChildProgress 업데이트 완료: childId={}, category={}", childId, category);
    }
}