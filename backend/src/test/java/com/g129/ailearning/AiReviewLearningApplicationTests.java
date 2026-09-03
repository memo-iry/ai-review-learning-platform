package com.skala.ailearning;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.skala.ailearning.ai.AiAnalysisRepository;
import com.skala.ailearning.ai.PersonalizedReviewRepository;
import com.skala.ailearning.quiz.QuizRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class AiReviewLearningApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private AiAnalysisRepository analysisRepository;
    @Autowired private PersonalizedReviewRepository reviewRepository;
    @Autowired private QuizRepository quizRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void exposesOpenApiDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("AI 맞춤형 복습 학습 API"))
                .andExpect(jsonPath("$.paths['/api/reflections']").exists());
    }

    @Test
    void createsReflectionAndPersistsAnalysisReviewAndQuiz() throws Exception {
        String body = """
                {
                  "userId": 2,
                  "lectureId": 1,
                  "understood": "Controller와 Service의 호출 흐름을 이해했습니다.",
                  "difficult": "Repository와 DTO의 책임 구분이 어렵습니다.",
                  "wantsToLearn": "계층별 책임을 더 공부하고 싶습니다."
                }
                """;

        mockMvc.perform(post("/api/reflections")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lectureId").value(1));

        mockMvc.perform(post("/api/reflections/1/analyze"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewMaterial.title").exists());

        mockMvc.perform(post("/api/reflections")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 이 강의에 작성한 회고록이 있습니다."));

        mockMvc.perform(post("/api/reflections/1/analyze"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 분석이 완료된 회고록입니다."));

        assertThat(analysisRepository.count()).isEqualTo(1);
        assertThat(reviewRepository.count()).isEqualTo(1);
        assertThat(quizRepository.count()).isEqualTo(1);
    }
}
