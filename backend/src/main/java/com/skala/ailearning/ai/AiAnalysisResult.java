package com.skala.ailearning.ai;

import java.util.List;

public record AiAnalysisResult(
        int understandingScore,
        String analysisReason,
        String understoodSummary,
        String weaknessSummary,
        List<String> understoodTopics,
        List<String> weakTopics,
        List<String> recommendedTopics,
        String reviewTitle,
        List<String> coreConcepts,
        String exampleCode,
        List<QuizItem> quiz
) {
    public record QuizItem(String question, String answer) {
    }
}
