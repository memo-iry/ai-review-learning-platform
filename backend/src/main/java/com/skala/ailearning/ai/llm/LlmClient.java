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
