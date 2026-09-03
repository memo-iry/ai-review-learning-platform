package com.skala.ailearning.ai;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record ReviewSummaryResponse(
        Long reviewId,
        Long quizId,
        String title,
        Long lectureId,
        String lectureTitle,
        LocalDate lectureDate,
        Integer understandingScore,
        List<String> focusTopics,
        String status,
        OffsetDateTime createdAt,
        Integer lastQuizScore,
        int attemptCount
) {
}
