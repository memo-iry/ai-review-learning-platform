package com.skala.ailearning.ai.llm;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PromptLoader {
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String load(String name) {
        return cache.computeIfAbsent(name, key -> {
            try (var stream = new ClassPathResource("prompts/" + key + ".md").getInputStream()) {
                return StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new LlmException("프롬프트를 읽지 못했습니다: " + key, e);
            }
        });
    }

    public String fill(String template, Map<String, String> values) {
        String filled = template;
        for (var entry : values.entrySet()) {
            String value = entry.getValue();
            filled = filled.replace("{{" + entry.getKey() + "}}",
                    value == null || value.isBlank() ? "없음" : value);
        }
        return filled;
    }
}
