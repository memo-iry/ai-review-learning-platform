package com.g129.ailearning.ai;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/reflections")
@Tag(name = "AI 복습 분석", description = "회고록 이해도 분석 및 맞춤형 복습자료 생성 API")
public class AiReviewController {

    private final AiReviewService aiReviewService;

    public AiReviewController(AiReviewService aiReviewService) {
        this.aiReviewService = aiReviewService;
    }

    @PostMapping("/{reflectionId}/analyze")
    @Operation(summary = "회고록 이해도 분석 및 복습자료 생성")
    public AnalysisResponse analyze(@PathVariable Long reflectionId) {
        return aiReviewService.analyze(reflectionId);
    }
}
