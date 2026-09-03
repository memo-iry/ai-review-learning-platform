package com.skala.ailearning.ai;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skala.ailearning.ai.AnalysisResponse.QuizItem;
import com.skala.ailearning.ai.AnalysisResponse.ReviewMaterial;
import com.skala.ailearning.material.LectureMaterial;
import com.skala.ailearning.material.LectureMaterialRepository;
import com.skala.ailearning.reflection.Reflection;
import com.skala.ailearning.reflection.ReflectionService;
import com.skala.ailearning.quiz.Quiz;
import com.skala.ailearning.quiz.QuizRepository;

@Primary
@Service
public class MockAiReviewService implements AiReviewService {

    private final ReflectionService reflectionService;
    private final LectureMaterialRepository materialRepository;
    private final AiAnalysisRepository analysisRepository;
    private final PersonalizedReviewRepository reviewRepository;
    private final QuizRepository quizRepository;
    private final ObjectMapper objectMapper;

    public MockAiReviewService(ReflectionService reflectionService,
                               LectureMaterialRepository materialRepository,
                               AiAnalysisRepository analysisRepository,
                               PersonalizedReviewRepository reviewRepository,
                               QuizRepository quizRepository,
                               ObjectMapper objectMapper) {
        this.reflectionService = reflectionService;
        this.materialRepository = materialRepository;
        this.analysisRepository = analysisRepository;
        this.reviewRepository = reviewRepository;
        this.quizRepository = quizRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public AnalysisResponse analyze(Long reflectionId) {
        Reflection reflection = reflectionService.getEntity(reflectionId);

        int totalLength = reflection.getUnderstood().length()
                + reflection.getDifficult().length();
        if (totalLength < 20) {
            return new AnalysisResponse(
                    reflectionId,
                    0,
                    "회고록 내용이 부족합니다.",
                    "이해한 내용과 어려웠던 내용을 조금 더 구체적으로 작성해 주세요.",
                    "Mock 분석 기준으로 회고록 본문이 20자 미만입니다.",
                    null);
        }

        List<LectureMaterial> materials = materialRepository
                .findByLectureIdOrderByIdAsc(reflection.getLecture().getId());
        int score = Math.min(95, 55 + Math.min(30, reflection.getUnderstood().length() / 3));
        String sourceNames = materials.stream()
                .map(LectureMaterial::getTitle)
                .limit(3)
                .reduce((left, right) -> left + ", " + right)
                .orElse("등록된 강의자료 없음");

        ReviewMaterial review = new ReviewMaterial(
                reflection.getLecture().getTitle() + " 맞춤형 복습자료",
                List.of(
                        "Controller, Service, Repository의 역할을 구분합니다.",
                        "REST API의 URI와 HTTP 메서드 설계 원칙을 복습합니다.",
                        "회고록에 작성한 어려운 내용을 강의자료와 연결합니다."),
                "@GetMapping(\"/api/lectures\")\npublic List<LectureResponse> findAll() {\n    return lectureService.findAll();\n}",
                List.of(
                        new QuizItem("REST API에서 조회에 사용하는 HTTP 메서드는?", "GET"),
                        new QuizItem("비즈니스 로직을 담당하는 계층은?", "Service")));

        saveAnalysisResult(reflection, review);

        return new AnalysisResponse(
                reflectionId,
                score,
                "작성한 개념의 핵심 흐름을 이해하고 있습니다.",
                reflection.getDifficult(),
                "Mock RAG가 다음 강의자료를 참조했습니다: " + sourceNames,
                review);
    }

    private void saveAnalysisResult(Reflection reflection, ReviewMaterial review) {
        try {
            AiAnalysis analysis = analysisRepository.save(new AiAnalysis(
                    reflection,
                    objectMapper.writeValueAsString(List.of("REST API", "Controller")),
                    objectMapper.writeValueAsString(List.of(reflection.getDifficult())),
                    objectMapper.writeValueAsString(List.of("계층별 책임", "DTO 변환")),
                    "회고와 강의자료를 기반으로 취약 개념을 분석했습니다.",
                    AnalysisStatus.COMPLETED));
            PersonalizedReview personalizedReview = reviewRepository.save(new PersonalizedReview(
                    analysis,
                    review.title(),
                    objectMapper.writeValueAsString(review.coreConcepts()),
                    objectMapper.writeValueAsString(review)));
            quizRepository.save(new Quiz(
                    personalizedReview,
                    review.title() + " 확인 Quiz",
                    objectMapper.writeValueAsString(review.quiz())));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("AI 분석 결과를 JSON으로 변환할 수 없습니다.", exception);
        }
    }
}
