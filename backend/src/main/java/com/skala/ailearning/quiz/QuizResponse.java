package com.skala.ailearning.quiz;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Quiz 조회용 응답.
 * 응시 전 화면에서 쓰이므로 정답(answerIndex)과 해설(explanation)은 포함하지 않는다.
 * 채점 결과(정답 포함)는 QuizAttemptResponse 를 사용한다.
 */
public record QuizResponse(
        Long quizId,
        String title,
        OffsetDateTime createdAt,
        List<QuestionItem> questions
) {
    public record QuestionItem(
            String conceptName,
            String question,
            List<String> options
    ) {
    }
}