package com.skala.ailearning.quiz;

import java.time.OffsetDateTime;
import java.util.List;

public record QuizResponse(
                Long quizId,
                Long lectureId,
                String title,
                OffsetDateTime createdAt,
                List<QuestionItem> questions) {
        public record QuestionItem(
                        String conceptName,
                        String question,
                        List<String> options) {
        }
}
