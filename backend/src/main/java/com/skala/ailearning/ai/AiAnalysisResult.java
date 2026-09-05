package com.skala.ailearning.ai;

import java.util.List;
import java.util.Map;

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
        List<QuizItem> quiz,

        Map<String, String> conceptSummaries
) {
    public record QuizItem(
            String conceptName,
            String question,
            List<String> options,
            int answerIndex,
            String explanation
    ) {
    }
}
