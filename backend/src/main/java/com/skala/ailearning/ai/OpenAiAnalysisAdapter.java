package com.skala.ailearning.ai;

import com.skala.ailearning.ai.llm.LlmClient;
import com.skala.ailearning.ai.llm.LlmException;
import com.skala.ailearning.ai.llm.LlmProperties;
import com.skala.ailearning.ai.llm.PromptLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Primary
@Profile("openai")
public class OpenAiAnalysisAdapter implements AiAnalysisPort {
    private static final Logger log = LoggerFactory.getLogger(OpenAiAnalysisAdapter.class);

    private final LlmClient llmClient;
    private final PromptLoader prompts;
    private final LlmProperties properties;
    private final MockAiAnalysisAdapter fallback;

    public OpenAiAnalysisAdapter(LlmClient llmClient,
                                 PromptLoader prompts,
                                 LlmProperties properties,
                                 MockAiAnalysisAdapter fallback) {
        this.llmClient = llmClient;
        this.prompts = prompts;
        this.properties = properties;
        this.fallback = fallback;
    }

    @Override
    public AiAnalysisResult analyze(AnalysisCommand command) {
        try {
            return callModel(command);
        } catch (RuntimeException e) {
            if (!properties.fallbackToRules()) {
                throw e;
            }

            log.warn("모델 분석에 실패해 규칙 기반 결과로 물러납니다: {}", e.getMessage());
            return fallback.analyze(command);
        }
    }

    private AiAnalysisResult callModel(AnalysisCommand command) {
        String context = retrieveContext(command);

        JsonNode analysis = runAnalyzer(command, context);
        List<String> weakTopics = strings(analysis.path("weakTopics"));
        Map<String, String> conceptSummaries = stringMap(analysis.path("conceptSummaries"));

        if (weakTopics.isEmpty()) {
            return assemble(analysis, weakTopics, conceptSummaries, null, List.of());
        }

        JsonNode review = runReviewGenerator(command, weakTopics, conceptSummaries, context);
        List<AiAnalysisResult.QuizItem> quiz = runQuizGenerator(weakTopics, conceptSummaries, context);

        return assemble(analysis, weakTopics, conceptSummaries, review, quiz);
    }

    private String retrieveContext(AnalysisCommand command) {
        List<String> titles = command.materialTitles();
        if (titles == null || titles.isEmpty()) {
            return "";
        }
        return "강의자료 목록: " + String.join(", ", titles);
    }

    private JsonNode runAnalyzer(AnalysisCommand command, String context) {
        String system = prompts.fill(prompts.load("reflection-analyzer"), Map.of(
                "lectureTitle", command.lectureTitle(),
                "lectureDescription", command.lectureDescription(),
                "materialTitles", String.join(", ", nullSafe(command.materialTitles())),
                "retrievedContext", context,
                "conceptMasterySummaries", "",
                "understood", command.understood(),
                "difficult", command.difficult(),
                "wantsToLearn", command.wantsToLearn()));

        return llmClient.completeJson(system, "위 회고를 분석해 주세요.");
    }

    private JsonNode runReviewGenerator(AnalysisCommand command,
                                        List<String> weakTopics,
                                        Map<String, String> conceptSummaries,
                                        String context) {
        String system = prompts.fill(prompts.load("review-generator"), Map.of(
                "lectureTitle", command.lectureTitle(),
                "weakTopics", String.join(", ", weakTopics),
                "conceptSummaries", flatten(conceptSummaries),
                "retrievedContext", context));

        return llmClient.completeJson(system, "위 취약 개념에 대한 복습자료를 만들어 주세요.");
    }

    private List<AiAnalysisResult.QuizItem> runQuizGenerator(List<String> weakTopics,
                                                             Map<String, String> conceptSummaries,
                                                             String context) {
        String system = prompts.fill(prompts.load("quiz-generator"), Map.of(
                "weakTopics", String.join(", ", weakTopics),
                "conceptSummaries", flatten(conceptSummaries),
                "retrievedContext", context));

        JsonNode node = llmClient.completeJson(system, "위 취약 개념에 대한 확인 문제를 만들어 주세요.");

        List<AiAnalysisResult.QuizItem> items = new ArrayList<>();
        for (JsonNode q : node.path("quiz")) {
            List<String> options = strings(q.path("options"));
            int answerIndex = q.path("answerIndex").asInt(-1);

            if (options.size() != 4 || answerIndex < 0 || answerIndex >= 4) {
                log.warn("형식이 맞지 않는 문항을 버립니다: {}", q.path("question").asString());
                continue;
            }

            items.add(new AiAnalysisResult.QuizItem(
                    q.path("conceptName").asString(),
                    q.path("question").asString(),
                    options,
                    answerIndex,
                    q.path("explanation").asString()));
        }

        if (items.isEmpty()) {
            throw new LlmException("쓸 수 있는 확인 문제를 만들지 못했습니다");
        }
        return items;
    }

    private AiAnalysisResult assemble(JsonNode analysis,
                                      List<String> weakTopics,
                                      Map<String, String> conceptSummaries,
                                      JsonNode review,
                                      List<AiAnalysisResult.QuizItem> quiz) {
        return new AiAnalysisResult(
                clampScore(analysis.path("understandingScore").asInt(0)),
                analysis.path("analysisReason").asString(),
                analysis.path("understoodSummary").asString(),
                analysis.path("weaknessSummary").asString(),
                strings(analysis.path("understoodTopics")),
                weakTopics,
                strings(analysis.path("recommendedTopics")),
                review == null ? null : review.path("reviewTitle").asString(),
                review == null ? List.of() : strings(review.path("coreConcepts")),
                review == null ? null : review.path("exampleCode").asString(),
                quiz,
                conceptSummaries);
    }

    private static int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }

    private static List<String> strings(JsonNode array) {
        List<String> values = new ArrayList<>();
        for (JsonNode node : array) {
            String value = node.asString();
            if (value != null && !value.isBlank()) {
                values.add(value);
            }
        }
        return values;
    }

    private static Map<String, String> stringMap(JsonNode object) {
        Map<String, String> values = new LinkedHashMap<>();
        object.propertyStream().forEach(entry ->
                values.put(entry.getKey(), entry.getValue().asString()));
        return values;
    }

    private static String flatten(Map<String, String> summaries) {
        if (summaries.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        summaries.forEach((concept, summary) ->
                sb.append("- ").append(concept).append(": ").append(summary).append('\n'));
        return sb.toString();
    }

    private static List<String> nullSafe(List<String> values) {
        return values == null ? List.of() : values;
    }
}
