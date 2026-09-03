package com.skala.ailearning.mastery;

import java.util.List;

public record AdminOverviewResponse(
        int learnerCount,
        int reflectionCount,
        int conceptCount,
        List<ConceptStat> concepts,
        List<ConceptStat> weakest
) {
    public record ConceptStat(
            String conceptName,
            int averageScore,
            int averageLevel,
            long learnerCount
    ) {
    }
}
