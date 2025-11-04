package com.sinabro.backend.progress.service; // (네 서비스 패키지 경로에 맞게)

import com.sinabro.backend.progress.dto.ProgressSummaryDto;
import com.sinabro.backend.progress.entity.Category;
import com.sinabro.backend.progress.entity.ChildProgress;
import com.sinabro.backend.progress.repository.ChildProgressRepository;
import com.sinabro.backend.stage.entity.ChildFruitStatus; // ⭐️ Import 추가
import com.sinabro.backend.stage.entity.LearningFruit;
import com.sinabro.backend.stage.entity.Stage;
import com.sinabro.backend.stage.repository.ChildFruitStatusRepository; // ⭐️ Import 추가
import com.sinabro.backend.stage.repository.LearningFruitRepository;
import com.sinabro.backend.stage.repository.StageRepository;
import com.sinabro.backend.user.entity.Child;
import com.sinabro.backend.user.repository.ChildRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 트랜잭션
public class ProgressQueryService {

    private final ChildRepository childRepository;
    private final ChildProgressRepository childProgressRepository;
    private final LearningFruitRepository learningFruitRepository;
    private final StageRepository stageRepository;
    // ⭐️ ChildFruitStatusRepository 주입 추가! (진행률 계산용)
    private final ChildFruitStatusRepository childFruitStatusRepository;

    public ProgressSummaryDto getProgressSummary(String childId) {
        log.info("[ProgressQueryService] 진행 상황 요약 조회 시작: childId={}", childId);

        Child child = childRepository.findById(childId)
                .orElseThrow(() -> {
                    log.warn("[ProgressQueryService] 자녀를 찾을 수 없음: childId={}", childId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "자녀를 찾을 수 없습니다.");
                });

        List<ChildProgress> progressList = childProgressRepository.findByChildId(childId);
        Map<Category, ChildProgress> progressMap = progressList.stream()
                .collect(Collectors.toMap(ChildProgress::getCategory, Function.identity()));

        // ⭐️ 모든 카테고리별 문자열 생성
        String lsRecent = getFruitDescription(progressMap.get(Category.listening_study), ChildProgress::getLastFruitId);
        String lsBest = getFruitDescription(progressMap.get(Category.listening_study), ChildProgress::getBestFruitId);
        String wsRecent = getFruitDescription(progressMap.get(Category.writing_study), ChildProgress::getLastFruitId);
        String wsBest = getFruitDescription(progressMap.get(Category.writing_study), ChildProgress::getBestFruitId);
        String lgRecent = getFruitDescription(progressMap.get(Category.listening_game), ChildProgress::getLastFruitId);
        String lgBest = getFruitDescription(progressMap.get(Category.listening_game), ChildProgress::getBestFruitId);
        String wgRecent = getFruitDescription(progressMap.get(Category.writing_game), ChildProgress::getLastFruitId);
        String wgBest = getFruitDescription(progressMap.get(Category.writing_game), ChildProgress::getBestFruitId);

        // ⭐️ 다음 레벨 진행률 계산 함수 호출
        Double progressToNext = calculateProgressToNextLevel(child);

        // ⭐️ DTO 빌드 시 모든 필드 포함
        ProgressSummaryDto summary = ProgressSummaryDto.builder()
                .progressToNextLevel(progressToNext)
                .listeningStudyRecent(lsRecent)
                .listeningStudyBest(lsBest)
                .writingStudyRecent(wsRecent)
                .writingStudyBest(wsBest)
                .listeningGameRecent(lgRecent)
                .listeningGameBest(lgBest)
                .writingGameRecent(wgRecent)
                .writingGameBest(wgBest)
                .build();

        log.info("[ProgressQueryService] 진행 상황 요약 조회 완료: childId={}", childId);
        return summary;
    }
    /**
     * "레벨 X 의 Y열매" 형식의 문자열을 반환합니다.
     */
    private String getFruitDescription(ChildProgress progress, Function<ChildProgress, String> fruitIdExtractor) {
        if (progress == null) return null;
        String fruitId = fruitIdExtractor.apply(progress);
        if (fruitId == null) return null;

        Optional<LearningFruit> fruitOpt = learningFruitRepository.findById(fruitId);
        if (fruitOpt.isEmpty()) return null;
        LearningFruit fruit = fruitOpt.get();

        Optional<Stage> stageOpt = stageRepository.findById(fruit.getStageId());
        if (stageOpt.isEmpty()) return null;
        Stage stage = stageOpt.get();

        // "초급", "중급", "고급" 문자열을 1, 2, 3 숫자로 변환
        int levelNum = getLevelNumberFromString(stage.getLevel());

        // ⭐️ [수정] "레벨 X 의 Y열매" 형식으로 변경 (예: "레벨 1 의 3열매")
        return String.format("레벨 %d 의 %d열매",
                levelNum, // 예: 1
                fruit.getSequenceInStage() // 예: 3
        );
    }

    /**
     * ⭐️ [추가] "초급" -> 1, "중급" -> 2, "고급" -> 3 변환 헬퍼
     */
    private int getLevelNumberFromString(String levelStr) {
        if (levelStr == null) return 0;
        switch (levelStr) {
            case "초급": return 1;
            case "중급": return 2;
            case "고급": return 3;
            default: return 0; // 알 수 없는 값은 0
        }
    }

    /**
     * ⭐️ 다음 레벨까지의 진행률 계산 (구현 완료!)
     * 현재 레벨의 학습(Study) 카테고리 총 열매 수 대비 완료(active)한 열매 수 비율 계산
     */
    private Double calculateProgressToNextLevel(Child child) {
        Integer currentLevelInt = child.getChildLevel();
        if (currentLevelInt == null || currentLevelInt <= 0) {
            log.warn("[ProgressQueryService] 유효하지 않은 자녀 레벨: {}. 진행률 0.0 반환.", currentLevelInt);
            return 0.0; // 레벨 정보 없으면 0%
        }
        if (currentLevelInt >= 3) {
            log.info("[ProgressQueryService] 자녀가 최고 레벨({})임. 진행률 1.0 반환.", currentLevelInt);
            return 1.0; // 최고 레벨(고급)이면 100%
        }

        String currentLevelStr = switch (currentLevelInt) {
            case 1 -> "초급";
            case 2 -> "중급";
            // case 3 -> "고급"; // 고급은 위에서 처리됨
            default -> null;
        };

        if (currentLevelStr == null) {
            log.error("[ProgressQueryService] 레벨 숫자({})를 문자열로 변환 불가.", currentLevelInt);
            return 0.0;
        }
        log.debug("[ProgressQueryService] 현재 레벨 진행률 계산 시작: childId={}, level={}({})",
                child.getChildId(), currentLevelStr, currentLevelInt);

        // 1. 현재 레벨의 모든 '학습(Study)' 스테이지 조회
        List<Stage> currentLevelStudyStages = stageRepository.findByCategoryAndLevelOrderByStageIdAsc(
                Stage.Category.listening_study, currentLevelStr); // 듣기 학습
        currentLevelStudyStages.addAll(stageRepository.findByCategoryAndLevelOrderByStageIdAsc(
                Stage.Category.writing_study, currentLevelStr)); // 쓰기 학습 추가

        if (currentLevelStudyStages.isEmpty()) {
            log.warn("[ProgressQueryService] 현재 레벨({})에 해당하는 학습 스테이지 없음. 진행률 0.0 반환.", currentLevelStr);
            return 0.0;
        }

        // 2. 해당 스테이지들의 모든 열매 ID 목록 만들기 + 총 개수 세기
        List<String> fruitIdsInLevel = currentLevelStudyStages.stream()
                .flatMap(stage -> learningFruitRepository.findByStageIdOrderBySequenceInStage(stage.getStageId()).stream())
                .map(LearningFruit::getFruitId)
                .toList();

        int totalFruitsInLevel = fruitIdsInLevel.size();
        if (totalFruitsInLevel == 0) {
            log.warn("[ProgressQueryService] 현재 레벨({}) 학습 스테이지에 열매 없음. 진행률 0.0 반환.", currentLevelStr);
            return 0.0;
        }
        log.debug("[ProgressQueryService] 현재 레벨 총 학습 열매 개수: {}", totalFruitsInLevel);

        // 3. 해당 열매들 중 자녀가 활성화(is_active=true)한 개수 세기
        //    (findByChildIdAndFruitIdInAndIsActiveTrue 같은 메서드 필요 or 직접 필터링)
        //    findByChildId 를 쓰고 필터링하는 방식 사용
        List<ChildFruitStatus> allChildStatuses = childFruitStatusRepository.findById_ChildId(child.getChildId());
        long completedFruitsInLevel = allChildStatuses.stream()
                .filter(status -> fruitIdsInLevel.contains(status.getId().getFruitId())) // 현재 레벨 열매인지 확인
                .filter(ChildFruitStatus::isActive) // 활성화 상태인지 확인
                .count();

        log.debug("[ProgressQueryService] 현재 레벨 완료 학습 열매 개수: {}", completedFruitsInLevel);

        // 4. 진행률 계산 (소수점 두 자리까지)
        double progress = (double) completedFruitsInLevel / totalFruitsInLevel;
        // 소수점 아래 두 자리까지 반올림 (선택 사항)
        // progress = Math.round(progress * 100.0) / 100.0;

        log.info("[ProgressQueryService] 다음 레벨 진행률 계산 완료: childId={}, progress={}", child.getChildId(), progress);
        return progress;
    }
}