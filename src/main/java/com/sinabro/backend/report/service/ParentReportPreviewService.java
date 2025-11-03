package com.sinabro.backend.report.service;

// --- 필요한 모든 클래스 Import ---

// Lombok
import com.sinabro.backend.record.repository.ListeningGameResultRepository;
import com.sinabro.backend.record.repository.ListeningRecordRepository;
import com.sinabro.backend.record.repository.WritingGameResultRepository;
import com.sinabro.backend.record.repository.WritingRecordRepository;
import lombok.RequiredArgsConstructor;

// Spring Framework
import org.springframework.stereotype.Service;

// Java Standard Library
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

// Sinabro Project - Custom Classes
import com.sinabro.backend.report.client.OpenAiClient;
import com.sinabro.backend.report.dto.ReportRequestDto;
import com.sinabro.backend.report.dto.ReportResponseDto;
import com.sinabro.backend.user.entity.Child;
import com.sinabro.backend.user.repository.ChildRepository;
import com.sinabro.backend.progress.entity.Category;
import com.sinabro.backend.progress.entity.ChildProgress;
import com.sinabro.backend.progress.repository.ChildProgressRepository;
import com.sinabro.backend.stage.entity.LearningFruit;
import com.sinabro.backend.stage.repository.LearningFruitRepository;
import com.sinabro.backend.stage.repository.StageRepository;
import com.sinabro.backend.record.entity.ListeningGameResult;
import com.sinabro.backend.record.entity.ListeningRecord;
import com.sinabro.backend.record.entity.WritingGameResult;
import com.sinabro.backend.record.entity.WritingRecord;
import com.sinabro.backend.reward.repository.ChildStickerRepository;


/**
 * @author Gemini
 * @description 부모용 AI 학습 리포트 생성을 담당하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class ParentReportPreviewService {

    // --- 의존성 주입 ---
    private final ChildRepository childRepository;
    private final ChildProgressRepository childProgressRepository;
    private final LearningFruitRepository learningFruitRepository;
    private final StageRepository stageRepository;
    private final ChildStickerRepository childStickerRepository;
    private final ListeningGameResultRepository listeningGameResultRepository;
    private final ListeningRecordRepository listeningRecordRepository;
    private final WritingGameResultRepository writingGameResultRepository;
    private final WritingRecordRepository writingRecordRepository;
    private final OpenAiClient openAiClient;

    /**
     * AI 학습 리포트 미리보기를 생성하는 메인 메서드
     */
    public ReportResponseDto createReportPreview(ReportRequestDto requestDto) {
        String childId = requestDto.getChildId();
        LocalDate reportDate = LocalDate.parse(requestDto.getDate(), DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDateTime startOfDay = reportDate.atStartOfDay();
        LocalDateTime endOfDay = reportDate.atTime(LocalTime.MAX);

        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found with ID: " + childId));

        String childName = child.getChildName();
        int childLevel = child.getChildLevel();
        int childAge = Period.between(LocalDate.parse(child.getChildBirth()), LocalDate.now()).getYears();

        String listeningStudyProgressText = getProgressText(child, Category.listening_study);
        String writingStudyProgressText = getProgressText(child, Category.writing_study);
        String listeningGameProgressText = getProgressText(child, Category.listening_game);
        String writingGameProgressText = getProgressText(child, Category.writing_game);

        String todayActivitiesSummary = generateTodayActivitiesSummary(childId, startOfDay, endOfDay);

        long todayStickerCount = childStickerRepository.countByChildIdAndObtainedAtBetween(childId, startOfDay, endOfDay);
        long totalStickerCount = childStickerRepository.countByChildId(childId);

        String prompt = buildPrompt(
                childName, childAge, childLevel,
                listeningStudyProgressText, writingStudyProgressText, listeningGameProgressText, writingGameProgressText,
                todayActivitiesSummary,
                (int) todayStickerCount, (int) totalStickerCount, reportDate
        );

        String gptResponse = openAiClient.generateReport(prompt);

        return new ReportResponseDto(gptResponse, (int) todayStickerCount);
    }

    /**
     * 자녀의 카테고리별 진행 상황을 "단계 이름, N번째 열매" 형태의 텍스트로 변환합니다.
     */
    private String getProgressText(Child child, Category category) {
        Optional<ChildProgress> progressOpt = childProgressRepository.findByChildIdAndCategory(child.getChildId(), category);

        if (progressOpt.isEmpty() || progressOpt.get().getLastFruitId() == null) {
            return "아직 시작하지 않았어요";
        }

        return learningFruitRepository.findById(progressOpt.get().getLastFruitId())
                .flatMap(fruit -> stageRepository.findById(fruit.getStageId())
                        // ✅ 수정된 부분: stage.getTitle() 대신 stage.getLevel()을 사용하여 "초급 단계" 처럼 표시
                        .map(stage -> String.format("%s 단계, %d번째 열매", stage.getLevel(), fruit.getSequenceInStage())))
                .orElse("정보를 불러올 수 없어요");
    }

    /**
     * 특정 날짜의 자녀 활동(학습, 게임) 기록을 요약된 텍스트로 생성합니다.
     */
    private String generateTodayActivitiesSummary(String childId, LocalDateTime start, LocalDateTime end) {
        StringBuilder summary = new StringBuilder();

        List<ListeningRecord> lsRecords = listeningRecordRepository.findByLsChildIdAndLsLearningDateBetween(childId, start, end);
        if (!lsRecords.isEmpty()) {
            summary.append(String.format("듣기 학습을 %d개 완료했습니다. ", lsRecords.size()));
        }

        List<ListeningGameResult> lgResults = listeningGameResultRepository.findByLgChildIdAndLgPlayDateBetween(childId, start, end);
        for (ListeningGameResult result : lgResults) {
            String fruitTitle = learningFruitRepository.findById(result.getFruitId()).map(LearningFruit::getTitle).orElse("알 수 없는");
            summary.append(String.format("'%s' 듣기 게임에서 %d/%d점을 받았고, %s했습니다. ", fruitTitle, result.getLgScore(), result.getTotalQuestions(), result.isSuccess() ? "성공" : "실패"));
        }

        List<WritingRecord> wsRecords = writingRecordRepository.findByWsChildIdAndWsLearningDateBetween(childId, start, end);
        if (!wsRecords.isEmpty()) {
            summary.append(String.format("쓰기 학습을 %d개 완료했습니다. ", wsRecords.size()));
        }

        List<WritingGameResult> wgResults = writingGameResultRepository.findByWgChildIdAndWgPlayDateBetween(childId, start, end);
        for (WritingGameResult result : wgResults) {
            String fruitTitle = learningFruitRepository.findById(result.getFruitId()).map(LearningFruit::getTitle).orElse("알 수 없는");
            summary.append(String.format("'%s' 쓰기 게임에서 %s했습니다. ", fruitTitle, result.isSuccess() ? "성공" : "실패"));
        }

        return summary.length() > 0 ? summary.toString() : "오늘은 학습 활동 기록이 없어요.";
    }

    /**
     * OpenAI GPT에게 전달할 전체 프롬프트를 생성합니다.
     */
    private String buildPrompt(String name, int age, int level, String lsText, String wsText, String lgText, String wgText, String todaySummary, int todayStickers, int totalStickers, LocalDate reportDate) {
        return String.format(
                "너는 5~7세 아이들을 위한 한국어 학습 플랫폼 '시나브로'의 AI 학습 리포트 생성기야. " +
                        "아래 데이터를 바탕으로 부모님이 아이의 학습 상황을 쉽고 긍정적으로 이해할 수 있도록, 친구처럼 따뜻한 말투로 리포트를 작성해 줘. " +
                        "아이의 강점과 약점을 자연스럽게 언급하고, 앞으로의 학습 방향을 격려하며 제안해 줘. 전체 내용은 4~5 문단으로 구성해줘.\n\n" +
                        "--- 아이 정보 ---\n" +
                        "이름: %s (%d세)\n" +
                        "현재 레벨: %d\n\n" +
                        "--- 전체 진행도 ---\n" +
                        "듣기 학습: %s\n" +
                        "쓰기 학습: %s\n" +
                        "듣기 게임: %s\n" +
                        "쓰기 게임: %s\n\n" +
                        "--- %s의 학습 요약 ---\n" +
                        "%s\n\n" +
                        "--- 보상 현황 ---\n" +
                        "오늘 새로 얻은 스티커: %d개\n" +
                        "지금까지 모은 총 스티커: %d개\n\n" +
                        "--- 리포트 작성 가이드 ---\n" +
                        "1. '%s 부모님께' 로 시작하며 아이의 전체적인 진행 상황을 요약해줘.\n" +
                        "2. 오늘의 학습 활동을 구체적으로 칭찬하며 어떤 점이 좋았는지 설명해줘.\n" +
                        "3. 데이터를 바탕으로 아이의 강점과 약점(보완하면 좋을 점)을 분석해줘. (예: '단어 인지 능력은 뛰어나지만, 긴 문장 듣기는 조금 더 연습하면 좋겠어요.')\n" +
                        "4. 보상 현황을 언급하며 학습에 대한 동기부여가 잘 되고 있음을 알려줘.\n" +
                        "5. 마지막으로 '시나브로 AI 드림'으로 끝맺어줘.",
                name, age, level, lsText, wsText, lgText, wgText, reportDate.format(DateTimeFormatter.ofPattern("M월 d일")), todaySummary, todayStickers, totalStickers, name
        );
    }
}