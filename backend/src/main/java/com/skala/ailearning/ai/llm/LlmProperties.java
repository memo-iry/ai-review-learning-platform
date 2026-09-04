package com.skala.ailearning.ai.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record LlmProperties(
        String apiKey,
        String baseUrl,
        String model,
        int timeoutSeconds,
        boolean fallbackToRules
) {
    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
