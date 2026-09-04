package com.skala.ailearning.ai;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 규칙 기반 분석기. 개념 사전에서 회고에 등장한 개념을 찾아 이해 / 취약으로 나눈다.
 *
 * 프로파일을 걸지 않는다. openai 프로파일에서도 이 빈은 살아 있어야 한다.
 * 모델 호출이 실패했을 때 물러날 곳이기 때문이다.
 * 어느 구현을 쓸지는 OpenAiAnalysisAdapter 의 @Primary 가 정한다.
 */
@Component
public class MockAiAnalysisAdapter implements AiAnalysisPort {

    private static final int BASE_SCORE = 55;
    private static final int PER_UNDERSTOOD = 9;
    private static final int PER_WEAK = 8;
    private static final int MIN_SCORE = 30;
    private static final int MAX_SCORE = 95;

    @Override
    public AiAnalysisResult analyze(AnalysisCommand command) {
        List<Concept> understood = specific(match(command.understood()));
        List<Concept> weak = specific(match(command.difficult()));
        List<Concept> wanted = specific(match(command.wantsToLearn()));

        understood.removeAll(weak);

        if (weak.isEmpty()) {
            weak = fallbackFor(command);
        }

        int score = clamp(BASE_SCORE
                + understood.size() * PER_UNDERSTOOD
                - weak.size() * PER_WEAK);

        List<String> understoodNames = names(understood);
        List<String> weakNames = names(weak);
        List<String> recommended = merge(weakNames, names(wanted));

        return new AiAnalysisResult(
                score,
                reason(command.lectureTitle(), understoodNames, weakNames),
                understoodSummary(understoodNames),
                weaknessSummary(weakNames),
                understoodNames,
                weakNames,
                recommended,
                command.lectureTitle() + " 맞춤 복습",
                corePoints(weak),
                weak.getFirst().exampleCode(),
                weak.stream().limit(3).map(MockAiAnalysisAdapter::toQuizItem).toList(),
                conceptSummaries(understood, weak, command)
        );
    }

    /**
     * 개념별 상태 서술. 학습자가 실제로 쓴 문장을 근거로 함께 남긴다.
     * Mock 이 없는 사실을 지어내지 않도록, 분석해서 알아낸 것(이해 / 취약)과
     * 그 판단의 근거가 된 회고 원문만 담는다.
     * 실제 LLM 어댑터로 교체하면 같은 자리에 모델이 쓴 서술이 들어간다.
     */
    private static Map<String, String> conceptSummaries(List<Concept> understood,
                                                        List<Concept> weak,
                                                        AnalysisCommand command) {
        Map<String, String> summaries = new LinkedHashMap<>();

        understood.forEach(concept -> summaries.put(concept.name(),
                "%s 강의 회고에서 이해한 개념으로 분류했다. 회고 원문: \"%s\""
                        .formatted(command.lectureTitle(), trim(command.understood()))));

        weak.forEach(concept -> summaries.put(concept.name(),
                "%s 강의 회고에서 보완이 필요한 개념으로 분류했다. 회고 원문: \"%s\" 복습 지점: %s"
                        .formatted(command.lectureTitle(),
                                trim(command.difficult()),
                                String.join(" / ", concept.reviewPoints()))));

        return summaries;
    }

    private static String trim(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String cleaned = text.strip().replaceAll("\\s+", " ");
        return cleaned.length() <= 120 ? cleaned : cleaned.substring(0, 120) + "…";
    }

    private static AiAnalysisResult.QuizItem toQuizItem(Concept concept) {
        Concept.Question q = concept.question();
        return new AiAnalysisResult.QuizItem(
                concept.name(), q.text(), q.options(), q.answerIndex(), q.explanation());
    }

    private List<Concept> match(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return Concept.ALL.stream()
                .filter(concept -> concept.matches(text))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private List<Concept> specific(List<Concept> matched) {
        return matched.stream()
                .filter(concept -> matched.stream().noneMatch(other ->
                        other != concept && other.name().contains(concept.name())))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private List<Concept> fallbackFor(AnalysisCommand command) {
        List<Concept> byLecture = match(command.lectureTitle() + " " + command.lectureDescription());
        if (!byLecture.isEmpty()) {
            return byLecture.subList(0, Math.min(2, byLecture.size()));
        }
        return List.of(Concept.ALL.getFirst());
    }

    private List<String> names(List<Concept> concepts) {
        return concepts.stream().map(Concept::name).toList();
    }

    private List<String> merge(List<String> first, List<String> second) {
        Set<String> merged = new LinkedHashSet<>(first);
        merged.addAll(second);
        return List.copyOf(merged);
    }

    private List<String> corePoints(List<Concept> weak) {
        return weak.stream()
                .limit(2)
                .flatMap(concept -> concept.reviewPoints().stream())
                .toList();
    }

    private String reason(String lectureTitle, List<String> understood, List<String> weak) {
        return "%s 회고를 분석했습니다. 이해한 개념 %d개, 보완이 필요한 개념 %d개를 확인했습니다."
                .formatted(lectureTitle, understood.size(), weak.size());
    }

    private String understoodSummary(List<String> understood) {
        if (understood.isEmpty()) {
            return "회고에서 확실히 이해했다고 판단할 개념을 찾지 못했습니다. 다음 회고에는 이해한 내용을 개념 이름과 함께 적어 주세요.";
        }
        return "%s 는 설명할 수 있는 수준으로 보입니다. 이 개념들은 복습에서 간단히만 다룹니다."
                .formatted(String.join(", ", understood));
    }

    private String weaknessSummary(List<String> weak) {
        return "%s 에 대한 이해가 아직 부족합니다. 이번 복습은 이 개념들을 중심으로 구성했습니다."
                .formatted(String.join(", ", weak));
    }

    private int clamp(int score) {
        return Math.max(MIN_SCORE, Math.min(MAX_SCORE, score));
    }
}
