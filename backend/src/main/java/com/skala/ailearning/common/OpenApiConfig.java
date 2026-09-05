package com.skala.ailearning.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("AI 후행 학습 플랫폼 API")
                .version("v1")
                .description("""
                        강의자료와 학습 회고를 함께 분석해 개인별 복습자료를 제공하는 서비스의 REST API.

                        AI 분석은 현재 Mock 어댑터로 동작한다. 교체 지점은 AiAnalysisPort 하나이며,
                        새 구현체에 @Profile("openai") 를 붙이면 이 API 규격을 바꾸지 않고 전환된다.
                        """));
    }
}
