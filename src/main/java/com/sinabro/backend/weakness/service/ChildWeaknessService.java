package com.sinabro.backend.weakness.service;

// --- 필요한 모든 클래스 Import ---

// Lombok
import lombok.RequiredArgsConstructor;

// Spring Framework
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

// Java Standard Library
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.List;

// Sinabro Project - Custom Classes
import com.sinabro.backend.stage.entity.LearningFruit;
import com.sinabro.backend.stage.repository.LearningFruitRepository;
import com.sinabro.backend.progress.entity.Category;
import com.sinabro.backend.record.entity.ListeningGameResult;
import com.sinabro.backend.record.repository.ListeningGameResultRepository;
import com.sinabro.backend.report.client.OpenAiClient;
import com.sinabro.backend.study.entity.StudyListeningContent;
import com.sinabro.backend.study.repository.StudyListeningContentRepository;
import com.sinabro.backend.user.entity.Child;
import com.sinabro.backend.weakness.entity.ChildWeakness;
import com.sinabro.backend.weakness.repository.ChildWeaknessRepository;


/**
 * @author Gemini
 * @description 자녀의 학습/게임 결과를 기반으로 취약점을 분석하고 관리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class ChildWeaknessService {

    // --- 의존성 주입 (Service가 필요로 하는 부품들) ---
    private final LearningFruitRepository learningFruitRepository;
    private final ListeningGameResultRepository listeningGameResultRepository;
    private final ChildWeaknessRepository childWeaknessRepository;
    private final OpenAiClient openAiClient;
    private final StudyListeningContentRepository studyListeningContentRepository;


    /**
     * 특정 아동이 완료한 학습/게임 열매를 기반으로 취약점을 분석하고 DB에 저장/업데이트합니다.
     * 이 메서드는 외부(예: 게임 결과 저장 서비스)에서 호출되는 메인 진입점입니다.
     * @param child 분석할 자녀 엔티티
     * @param completedFruitId 방금 완료한 열매의 ID
     */
    public void analyzeAndUpsertWeakness(Child child, String completedFruitId) {

        LearningFruit fruit = learningFruitRepository.findById(completedFruitId)
                .orElseThrow(() -> new RuntimeException("Error: Fruit not found with ID " + completedFruitId));
        Category category = fruit.getCategory();

        String subjectTag = getSubjectTagForFruit(completedFruitId, category);

        BigDecimal weaknessScore = calculateWeaknessScore(child.getChildId(), completedFruitId, category);

        String analysisText = generateAnalysisText(child, subjectTag, weaknessScore);

        upsertWeakness(child, category, subjectTag, weaknessScore, analysisText);
    }

    /**
     * 카테고리에 맞는 콘텐츠/문제 테이블을 조회하여 주제 태그(subjectTag)를 반환합니다.
     * @param fruitId 열매 ID
     * @param category 열매의 카테고리
     * @return 조회된 주제 태그 문자열
     */
    private String getSubjectTagForFruit(String fruitId, Category category) {
        switch (category) {
            case listening_study:
                List<StudyListeningContent> contents = studyListeningContentRepository.findByFruit_FruitIdOrderByContentOrderAsc(fruitId);

                if (contents.isEmpty()) {
                    return "알 수 없는 주제";
                }

                // ✅ 수정된 부분: .name()을 추가해서 Enum을 String으로 변환
                return contents.get(0).getLsSubjectTag().name();

            case listening_game:
                // TODO: 듣기 게임 로직 추가
                return "듣기 게임 주제";

            default:
                throw new IllegalArgumentException("지원하지 않는 카테고리입니다: " + category);
        }
    }

    /**
     * 카테고리별로 분기하여 최근 3회 결과를 가져오고, 실패율(취약도)을 계산합니다.
     * @param childId 자녀 ID
     * @param fruitId 열매 ID
     * @param category 열매의 카테고리
     * @return 계산된 취약도 점수 (0.00 ~ 1.00)
     */
    private BigDecimal calculateWeaknessScore(String childId, String fruitId, Category category) {
        Pageable limit = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "lgPlayDate"));

        List<Boolean> recentSuccesses;

        switch (category) {
            case listening_game:
                recentSuccesses = listeningGameResultRepository.findRecentSuccessByChildIdAndFruitId(childId, fruitId, limit);
                break;
            default:
                return BigDecimal.ZERO;
        }

        if (recentSuccesses.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long failureCount = recentSuccesses.stream().filter(success -> !success).count();
        return new BigDecimal(failureCount).divide(new BigDecimal(recentSuccesses.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * 분석된 취약점 정보를 DB에 저장하거나 업데이트합니다. (UPSERT)
     * @param child 자녀 엔티티
     * @param category 카테고리
     * @param subjectTag 주제 태그
     * @param score 취약도 점수
     * @param text GPT 분석 텍스트
     */
    private void upsertWeakness(Child child, Category category, String subjectTag, BigDecimal score, String text) {
        ChildWeakness weakness = childWeaknessRepository.findByChildAndCategoryAndSubjectTag(child, category, subjectTag)
                .orElse(new ChildWeakness());

        weakness.setChild(child);
        weakness.setCategory(category);
        weakness.setSubjectTag(subjectTag);
        weakness.setWeaknessScore(score);
        weakness.setAnalysisText(text);
        weakness.setAnalyzedAt(new Timestamp(System.currentTimeMillis()));

        childWeaknessRepository.save(weakness);
    }

    /**
     * GPT에게 보낼 프롬프트를 만들고, API를 호출하여 분석 텍스트를 받아옵니다.
     * @param child 자녀 엔티티
     * @param subjectTag 주제 태그
     * @param score 취약도 점수
     * @return GPT가 생성한 분석 텍스트
     */
    private String generateAnalysisText(Child child, String subjectTag, BigDecimal score) {
        String prompt = String.format(
                "너는 유아 한국어 학습 전문가야. '%s' 아이가 '%s' 주제에서 보인 취약도 점수는 %.2f (1.0에 가까울수록 취약)이야. " +
                        "이 정보를 바탕으로, 부모님이 이해하기 쉽게 아이의 현재 상태를 진단하고 격려와 함께 간단한 학습 활동을 추천하는 문장을 1~2개로 생성해 줘.",
                child.getChildName(), subjectTag, score
        );
        return openAiClient.generateReport(prompt);
    }
}