package com.sinabro.backend.report.service;

import com.sinabro.backend.ai.OpenAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParentReportPreviewService {

    private final OpenAiClient openAiClient;

    public String generatePreview(String childId, String date) {
        // TODO: 여기서 childId 기반으로 DB 조회 → 학습 진도, 결과 가져오기
        // 지금은 임시 하드코딩 테스트

        String prompt = """
                아이 이름: %s
                날짜: %s
                학습 진행 상황: 듣기 학습 열매 5, 쓰기 학습 열매 6, 듣기 게임 열매 5, 쓰기 게임 열매 5
                위 정보를 바탕으로 부모님께 전달할 학습 리포트를 작성해줘.
                조건:
                - 아이 이름을 포함
                - 칭찬과 제안 포함
                """.formatted(childId, date);

        return openAiClient.chat(prompt);
    }
}
