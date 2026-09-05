package com.skala.ailearning.quiz;

import java.time.OffsetDateTime;

public record QuizAttemptSummaryResponse(
        Long attemptId,
        Long quizId,
        String quizTitle,
        Long lectureId,
        Integer score,
        Integer correctCount,
        Integer totalCount,
        OffsetDateTime completedAt
) {
}
