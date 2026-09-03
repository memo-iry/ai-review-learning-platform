package com.skala.ailearning.quiz;

import java.time.OffsetDateTime;

/**
 * "지난 퀴즈 불러오기" 목록에서 쓰는, 응시 이력 요약 응답.
 * 채점 상세(문항별 정답/해설)는 필요 없고, 카드에 보여줄 정도의 정보만 담는다.
 */
public record QuizAttemptSummaryResponse(
        Long attemptId,
        Long quizId,
        String quizTitle,
        Long lectureId,
        Integer score,
        Integer correctCount,
        Integer totalCount,
        OffsetDateTime completedAt
) {
}