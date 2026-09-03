package com.skala.ailearning.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI aiReviewLearningOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI 맞춤형 복습 학습 API")
                        .description("강의자료, 회고록, 이해도 분석 및 맞춤형 복습자료 API 문서")
                        .version("v1"));
    }
}
