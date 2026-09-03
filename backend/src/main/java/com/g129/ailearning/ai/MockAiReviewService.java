package com.g129.ailearning.ai;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g129.ailearning.ai.AnalysisResponse.QuizItem;
import com.g129.ailearning.ai.AnalysisResponse.ReviewMaterial;
import com.g129.ailearning.material.LearningDocument;
import com.g129.ailearning.material.LearningDocumentRepository;
import com.g129.ailearning.reflection.Reflection;
import com.g129.ailearning.reflection.ReflectionRepository;
import com.g129.ailearning.reflection.ReflectionService;

@Primary
@Service
public class MockAiReviewService implements AiReviewService {

    private final ReflectionService reflectionService;
    private final ReflectionRepository reflectionRepository;
    private final LearningDocumentRepository documentRepository;

    public MockAiReviewService(ReflectionService reflectionService,
                               ReflectionRepository reflectionRepository,
                               LearningDocumentRepository documentRepository) {
        this.reflectionService = reflectionService;
        this.reflectionRepository = reflectionRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    @Transactional
    public AnalysisResponse analyze(Long reflectionId) {
        Reflection reflection = reflectionService.getEntity(reflectionId);

        int totalLength = reflection.getUnderstoodContent().length()
                + reflection.getDifficultContent().length();
        if (totalLength < 20) {
            reflection.markNeedsRevision();
            reflectionRepository.save(reflection);
            return new AnalysisResponse(
                    reflectionId,
                    0,
                    "회고록 내용이 부족합니다.",
                    "이해한 내용과 어려웠던 내용을 조금 더 구체적으로 작성해 주세요.",
                    "Mock 분석 기준으로 회고록 본문이 20자 미만입니다.",
                    null);
        }

        List<LearningDocument> documents = documentRepository
                .findByCourseIdOrderByIdAsc(reflection.getCourse().getId());
        int score = Math.min(95, 55 + Math.min(30, reflection.getUnderstoodContent().length() / 3));
        String sourceNames = documents.stream()
                .map(LearningDocument::getName)
                .limit(3)
                .reduce((left, right) -> left + ", " + right)
                .orElse("등록된 강의자료 없음");

        reflection.markAnalyzed();
        reflectionRepository.save(reflection);

        ReviewMaterial review = new ReviewMaterial(
                reflection.getCourse().getName() + " 맞춤형 복습자료",
                List.of(
                        "Controller, Service, Repository의 역할을 구분합니다.",
                        "REST API의 URI와 HTTP 메서드 설계 원칙을 복습합니다.",
                        "회고록에 작성한 어려운 내용을 강의자료와 연결합니다."),
                "@GetMapping(\"/api/courses\")\npublic List<CourseResponse> findAll() {\n    return courseService.findAll();\n}",
                List.of(
                        new QuizItem("REST API에서 조회에 사용하는 HTTP 메서드는?", "GET"),
                        new QuizItem("비즈니스 로직을 담당하는 계층은?", "Service")));

        return new AnalysisResponse(
                reflectionId,
                score,
                "작성한 개념의 핵심 흐름을 이해하고 있습니다.",
                reflection.getDifficultContent(),
                "Mock RAG가 다음 강의자료를 참조했습니다: " + sourceNames,
                review);
    }
}

