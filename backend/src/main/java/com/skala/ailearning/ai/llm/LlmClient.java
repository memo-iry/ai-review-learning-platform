package com.skala.ailearning.ai.llm;

import org.springframework.http.MediaType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * OpenAI Chat Completions 를 직접 호출한다.
 *
 * Spring AI 를 쓰지 않는 이유는 이 프로젝트가 Boot 4.1 이기 때문이다.
 * Spring AI 1.1 은 Spring 6 기준이라 spring-context 가 7 로 강제 승급되고
 * Jackson 2 를 따로 끌고 온다. 검증되지 않은 조합을 피한다.
 * 호출 자체는 JSON 한 번 주고받는 일이라 직접 부르는 편이 단순하다.
 */
@Component
@Profile("openai")
public class LlmClient {

    private final RestClient restClient;
    private final LlmProperties properties;
    private final ObjectMapper objectMapper;

    public LlmClient(LlmProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.apiKey())
                .requestFactory(timeoutFactory(properties.timeoutSeconds()))
                .build();
    }

    private static org.springframework.http.client.ClientHttpRequestFactory timeoutFactory(int seconds) {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(seconds));
        return factory;
    }

    /**
     * 시스템 프롬프트를 보내고 JSON 응답을 받는다.
     * response_format 을 json_object 로 두어 모델이 코드펜스나 설명을 덧붙이지 못하게 한다.
     */
    public JsonNode completeJson(String systemPrompt, String userMessage) {
        Map<String, Object> body = Map.of(
                "model", properties.model(),
                "temperature", 0.2,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)));

        String raw = restClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        JsonNode response = objectMapper.readTree(raw);
        String content = response.path("choices").path(0).path("message").path("content").asString();

        if (content == null || content.isBlank()) {
            throw new LlmException("모델이 빈 응답을 돌려주었습니다");
        }

        return objectMapper.readTree(content);
    }
}
