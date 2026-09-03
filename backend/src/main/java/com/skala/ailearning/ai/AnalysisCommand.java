package com.skala.ailearning.ai;

import java.util.List;

public record AnalysisCommand(
        String lectureTitle,
        String lectureDescription,
        List<String> materialTitles,
        String understood,
        String difficult,
        String wantsToLearn
) {
}
