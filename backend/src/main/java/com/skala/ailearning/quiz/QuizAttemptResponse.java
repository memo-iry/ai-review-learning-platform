package com.skala.ailearning.quiz;

import java.util.List;

public record QuizAttemptResponse(
        Long attemptId,
        int score,
        int correctCount,
        int totalCount,
        List<Graded> results,
        List<MasteryChange> masteryChanges,
        int levelBefore,
        int levelAfter
) {
    public record Graded(
            String conceptName,
            String question,
            List<String> options,
            int selectedIndex,
            int answerIndex,
            boolean correct,
            String explanation
    ) {
    }

    public record MasteryChange(
            String conceptName,
            int scoreBefore,
            int scoreAfter,
            int levelBefore,
            int levelAfter
    ) {
    }
}
