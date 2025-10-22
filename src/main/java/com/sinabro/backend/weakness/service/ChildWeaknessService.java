package com.sinabro.backend.weakness.service;

// --- 필요한 모든 클래스 Import ---

// Lombok
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Spring Framework
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Java Standard Library
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.List;

// Sinabro Project - Custom Classes
import com.sinabro.backend.stage.entity.LearningFruit;
import com.sinabro.backend.stage.repository.LearningFruitRepository;
import com.sinabro.backend.progress.entity.Category;
import com.sinabro.backend.report.client.OpenAiClient;
import com.sinabro.backend.user.entity.Child;
import com.sinabro.backend.weakness.entity.ChildWeakness;
import com.sinabro.backend.weakness.repository.ChildWeaknessRepository;

// --- ⬇️ 4가지 카테고리 지원을 위해 필요한 Repository들 ⬇️ ---
// (이 파일들이 네 프로젝트에 실제 있어야 해!)

// 1. Record (결과) Repositories
import com.sinabro.backend.record.repository.ListeningGameResultRepository;
import com.sinabro.backend.record.repository.ListeningRecordRepository;
import com.sinabro.backend.record.repository.WritingGameResultRepository;
import com.sinabro.backend.record.repository.WritingRecordRepository;

// 2. Content/Question (주제 태그 조회용) Repositories
import com.sinabro.backend.study.repository.StudyListeningContentRepository;
import com.sinabro.backend.study.repository.StudyWritingContentRepository;
// ⭐️ 네가 제공한 실제 게임 Repository 임포트
import com.sinabro.backend.game.listening.repository.ListeningGameQuestionRepository;
import com.sinabro.backend.game.writing.repository.WritingGameQuestionRepository;


// 3. Content/Question (주제 태그 조회용) Entities
import com.sinabro.backend.study.entity.StudyListeningContent;
import com.sinabro.backend.study.entity.StudyWritingContent;
// ⭐️ 네가 제공한 실제 게임 Entity 임포트
import com.sinabro.backend.game.listening.entity.ListeningGameQuestion;
import com.sinabro.backend.game.writing.entity.WritingGameQuestion;


/**
 * @author Gemini
 * @description 자녀의 학습/게임 결과를 기반으로 취약점을 분석하고 관리하는 서비스 클래스
 * @version 2.1 (Game Repository 실제 메서드명 반영)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChildWeaknessService {

    // --- ⬇️ 의존성 주입 (4개 카테고리 지원을 위해 모두 추가) ⬇️ ---

    // 공통
    private final LearningFruitRepository learningFruitRepository;
    private final ChildWeaknessRepository childWeaknessRepository;
    private final OpenAiClient openAiClient;

    // 1. Record (결과) Repositories
    private final ListeningRecordRepository listeningRecordRepository;
    private final WritingRecordRepository writingRecordRepository;
    private final ListeningGameResultRepository listeningGameResultRepository;
    private final WritingGameResultRepository writingGameResultRepository;

    // 2. Content/Question (주제 태그 조회용) Repositories
    private final StudyListeningContentRepository studyListeningContentRepository;
    private final StudyWritingContentRepository studyWritingContentRepository;
    // ⭐️ 실제 게임 Repository
    private final ListeningGameQuestionRepository listeningGameQuestionRepository;
    private final WritingGameQuestionRepository writingGameQuestionRepository;


    /**
     * 특정 아동이 완료한 학습/게임 열매를 기반으로 취약점을 분석하고 DB에 저장/업데이트합니다.
     */
    @Transactional
    public void analyzeAndUpsertWeakness(Child child, String completedFruitId) {
        log.info("[WeaknessService] 취약점 분석 시작: childId={}, fruitId={}", child.getChildId(), completedFruitId);

        LearningFruit fruit = learningFruitRepository.findById(completedFruitId)
                .orElseThrow(() -> new RuntimeException("Error: Fruit not found with ID " + completedFruitId));
        Category category = fruit.getCategory();

        // ⭐️ 수정된 메서드 호출
        String subjectTag = getSubjectTagForFruit(completedFruitId, category);
        log.info("[WeaknessService] 카테고리: {}, 주제 태그: {}", category, subjectTag);

        BigDecimal weaknessScore = calculateWeaknessScore(child.getChildId(), completedFruitId, category);
        log.info("[WeaknessService] 취약도 점수: {}", weaknessScore);

        String analysisText = generateAnalysisText(child, subjectTag, weaknessScore);

        upsertWeakness(child, category, subjectTag, weaknessScore, analysisText);
        log.info("[WeaknessService] 취약점 분석 완료 및 저장: childId={}, subjectTag={}", child.getChildId(), subjectTag);
    }

    /**
     * [⭐ 수정됨] 카테고리에 맞는 콘텐츠/문제 테이블을 조회하여 주제 태그(subjectTag)를 반환합니다.
     * (네가 준 게임 Repository의 실제 메서드명을 반영했어!)
     */
    private String getSubjectTagForFruit(String fruitId, Category category) {

        switch (category) {
            case listening_study: // 👈 이게 123번째 줄 에러
                List<StudyListeningContent> lsContents = studyListeningContentRepository.findByFruit_FruitIdOrderByContentOrderAsc(fruitId);
                if (lsContents.isEmpty()) return "알 수 없는 주제";

                // ⬇️ ⬇️ ⬇️ .name() 다시 추가!!! ⬇️ ⬇️ ⬇️
                return lsContents.get(0).getLsSubjectTag().name();

            case writing_study:
                List<StudyWritingContent> wsContents = studyWritingContentRepository.findByFruit_FruitIdOrderByContentOrderAsc(fruitId);
                if (wsContents.isEmpty()) return "알 수 없는 주제";

                // ⬇️ ⬇️ ⬇️ .name() 다시 추가!!! ⬇️ ⬇️ ⬇️
                return wsContents.get(0).getWsSubjectTag().name();

            case listening_game: // 👈 이게 134번째 줄 에러였음
                List<ListeningGameQuestion> lgQuestions = listeningGameQuestionRepository.findByFruitIdOrderByLgContentOrderAsc(fruitId);
                if (lgQuestions.isEmpty()) return "알 수 없는 주제";

                // ⬇️ ⬇️ ⬇️ 여기는 .name()이 없는 게 맞아! ⬇️ ⬇️ ⬇️
                return lgQuestions.get(0).getLgSubjectTag();

            case writing_game:
                List<WritingGameQuestion> wgQuestions = writingGameQuestionRepository.findByFruitId(fruitId);
                if (wgQuestions.isEmpty()) return "알 수 없는 주제";

                // ⬇️ ⬇️ ⬇️ 여기도 .name()이 없는 게 맞을 거야! ⬇️ ⬇️ ⬇️
                return wgQuestions.get(0).getWgSubjectTag().name();

            default:
                log.error("[WeaknessService] 지원하지 않는 카테고리입니다: {}", category);
                throw new IllegalArgumentException("지원하지 않는 카테고리입니다: " + category);
        }
    }

    /**
     * 카테고리별로 분기하여 최근 3회 결과를 가져오고, 실패율(취약도)을 계산합니다.
     * (이 부분은 수정되지 않았어. 아래 '중요!' 부분을 확인해 줘!)
     */
    private BigDecimal calculateWeaknessScore(String childId, String fruitId, Category category) {

        // ❌ 이 줄을 지우고 -> Pageable limit = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Boolean> recentResults;

        switch (category) {
            case listening_study:
                // ⭐️ 1. '듣기 학습' 날짜 필드명으로 수정
                Pageable lsLimit = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "lsLearningDate"));
                recentResults = listeningRecordRepository.findRecentCompletedByChildIdAndFruitId(childId, fruitId, lsLimit);
                break;

            case writing_study:
                // ⭐️ 2. '쓰기 학습' 날짜 필드명으로 수정 (에러난 곳!)
                Pageable wsLimit = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "wsLearningDate"));
                recentResults = writingRecordRepository.findRecentCompletedByChildIdAndFruitId(childId, fruitId, wsLimit);
                break;

            case listening_game:
                // ⭐️ 3. '듣기 게임' 날짜 필드명으로 수정 (이건 "lgPlayDate"가 맞았지!)
                Pageable lgLimit = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "lgPlayDate"));
                recentResults = listeningGameResultRepository.findRecentSuccessByChildIdAndFruitId(childId, fruitId, lgLimit);
                break;

            case writing_game:
                // ⭐️ 4. '쓰기 게임' 날짜 필드명으로 수정 (이것도 확인해 봐, 아마 "wgPlayDate"?)
                Pageable wgLimit = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "wgPlayDate"));
                recentResults = writingGameResultRepository.findRecentSuccessByChildIdAndFruitId(childId, fruitId, wgLimit);
                break;

            default:
                throw new IllegalArgumentException("지원하지 않는 카테고리입니다: " + category);
        }

        // --- (아래 계산 로직은 동일) ---
        if (recentResults == null || recentResults.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long failureCount = recentResults.stream().filter(result -> !result).count();
        return new BigDecimal(failureCount).divide(new BigDecimal(recentResults.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * 분석된 취약점 정보를 DB에 저장하거나 업데이트합니다. (UPSERT)
     * (이 메서드는 수정할 필요 없음!)
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
     * (이 메서드는 수정할 필요 없음!)
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