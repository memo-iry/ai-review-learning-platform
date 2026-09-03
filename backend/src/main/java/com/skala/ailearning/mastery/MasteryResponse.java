package com.skala.ailearning.mastery;

import java.time.OffsetDateTime;
import java.util.List;

public record MasteryResponse(
        Long userId,
        int currentLevel,
        int averageScore,
        int progressRate,
        int reviewedLectures,
        int totalLectures,
        int conceptCount,
        List<ConceptScore> concepts,
        List<ConceptScore> weakest
) {
    public record ConceptScore(
            String conceptName,
            int level,
            int score,
            OffsetDateTime updatedAt
    ) {
        static ConceptScore from(ConceptMastery mastery) {
            return new ConceptScore(
                    mastery.getConceptName(),
                    mastery.getLevel(),
                    mastery.getScore() == null ? 0 : mastery.getScore(),
                    mastery.getUpdatedAt()
            );
        }
    }
}
