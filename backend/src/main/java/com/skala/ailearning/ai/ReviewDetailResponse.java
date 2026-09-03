package com.skala.ailearning.ai;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record ReviewDetailResponse(
        Long reviewId,
        Long quizId,
        String title,
        Long lectureId,
        String lectureTitle,
        LocalDate lectureDate,
        Integer understandingScore,
        String analysisReason,
        String understoodSummary,
        String weaknessSummary,
        List<String> understoodTopics,
        List<String> weakTopics,
        List<String> coreConcepts,
        String exampleCode,
        List<AnalysisResponse.QuizItem> quiz,
        String status,
        OffsetDateTime createdAt,
        List<Attempt> attempts
) {
    public record Attempt(
            Long attemptId,
            Integer score,
            int correctCount,
            int totalCount,
            OffsetDateTime completedAt
    ) {
    }
}
