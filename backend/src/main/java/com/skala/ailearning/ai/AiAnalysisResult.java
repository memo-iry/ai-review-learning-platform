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

        /**
         * 개념 이름 → 그 개념에서 이 학습자가 지금 어떤 상태인지에 대한 서술.
         * concept_mastery.summary 에 저장되어 이후 복습자료 생성 프롬프트에 실린다.
         * 서술을 만드는 책임은 이 인터페이스의 구현체에 있다 — 영속 계층이 합성하지 않는다.
         */
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
