package com.sinabro.backend.report.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class OpenAiClient {

    private final RestTemplate restTemplate;

    // ✅ 이 키 하나면 충분함 (프로젝트 키 그대로 Bearer로 사용)
    @Value("${openai.api-key}")
    private String apiKey;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    public OpenAiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateReport(String prompt) {
        // === 요청 헤더 설정 ===
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey); // ✅ Bearer 인증에 프로젝트 키 사용

        // === 요청 바디 구성 ===
        Message message = new Message("user", prompt);
        OpenAiRequest requestBody = new OpenAiRequest("gpt-4o-mini", List.of(message));

        HttpEntity<OpenAiRequest> requestEntity = new HttpEntity<>(requestBody, headers);

        // === 실제 API 호출 ===
        OpenAiResponse response = restTemplate.postForObject(OPENAI_URL, requestEntity, OpenAiResponse.class);

        // === 응답 검증 ===
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            return "리포트 생성에 실패했습니다.";
        }

        return response.choices().get(0).message().content();
    }
}

// ✅ OpenAI API용 DTO
record OpenAiRequest(String model, List<Message> messages) {}
record Message(String role, String content) {}
record OpenAiResponse(List<Choice> choices) {}
record Choice(Message message) {}
