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

/**
 * 실제 모델로 회고를 분석한다.
 *
 * 프롬프트를 셋으로 나누되 포트는 하나로 둔다. 셋을 여기서 부르고 하나의 결과로 조립하므로
 * Controller · Service · AnalysisCommand · AiAnalysisResult 는 그대로다.
 *
 * 검색(RAG)은 아직 붙이지 않았다. 붙일 자리는 {@link #retrieveContext} 하나이고,
 * 그 자리가 채워져도 바깥 계약은 바뀌지 않는다.
 */
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
            // 분석이 실패했다고 화면이 비면 안 된다. 규칙 기반 결과로 물러난다.
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
            // 취약 개념이 없으면 복습자료와 확인 문제를 만들 대상이 없다.
            // 억지로 만들지 않고 분석만 돌려준다. 서비스가 이 상태를 화면에 알린다.
            return assemble(analysis, weakTopics, conceptSummaries, null, List.of());
        }

        JsonNode review = runReviewGenerator(command, weakTopics, conceptSummaries, context);
        List<AiAnalysisResult.QuizItem> quiz = runQuizGenerator(weakTopics, conceptSummaries, context);

        return assemble(analysis, weakTopics, conceptSummaries, review, quiz);
    }

    /**
     * 검색된 강의자료 본문이 들어올 자리.
     *
     * 지금은 강의자료 제목까지만 넘긴다. Vector Store 를 붙이면 여기서
     * 회고로 질의를 만들고 lecture_id 로 범위를 좁혀 검색한다.
     * 검색 결과는 어댑터 밖으로 나가지 않으므로 AnalysisCommand 를 바꿀 필요가 없다.
     */
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

            // 보기가 4개가 아니거나 정답 위치가 범위를 벗어나면 채점할 수 없다. 그 문항만 버린다.
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
