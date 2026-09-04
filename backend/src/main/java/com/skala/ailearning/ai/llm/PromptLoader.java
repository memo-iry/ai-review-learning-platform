package com.skala.ailearning.ai.llm;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 프롬프트를 리소스 파일에서 읽는다.
 * 코드에 박으면 문구를 고칠 때마다 재컴파일해야 하고, 리뷰할 때 diff 가 코드에 섞인다.
 */
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

    /** {{name}} 자리를 값으로 바꾼다. 값이 없으면 "없음" 으로 채워 빈 자리를 남기지 않는다. */
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
