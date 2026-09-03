package com.skala.ailearning.quiz;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record QuizAttemptRequest(
        @NotNull(message = "userId 는 필수입니다")
        Long userId,

        @NotEmpty(message = "답안을 선택해 주세요")
        List<Integer> answers
) {
}
