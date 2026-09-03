package com.skala.ailearning.ai;

import java.util.List;

public record AnalysisResponse(
        Long reflectionId,
        int understandingScore,
        String understoodSummary,
        String weaknessSummary,
        String analysisReason,
        ReviewMaterial reviewMaterial) {

    public record ReviewMaterial(
            String title,
            List<String> coreConcepts,
            String exampleCode,
            List<QuizItem> quiz) {
    }

    public record QuizItem(String question, String answer) {
    }
}

