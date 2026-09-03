package com.skala.ailearning.ai;

import java.util.List;

public record AnalysisResponse(
        Long analysisId,
        Integer understandingScore,
        String analysisReason,
        String understoodSummary,
        String weaknessSummary,
        List<String> understoodTopics,
        List<String> weakTopics,
        List<String> recommendedTopics,
        int levelBefore,
        int levelAfter,
        ReviewMaterial reviewMaterial
) {
    public record ReviewMaterial(
            Long reviewId,
            String title,
            List<String> coreConcepts,
            String exampleCode,
            List<QuizItem> quiz
    ) {
    }

    public record QuizItem(String question, String answer) {
    }
}
