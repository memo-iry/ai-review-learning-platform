package com.skala.ailearning.reflection;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReflectionRequest(
        @NotNull(message = "userId 는 필수입니다")
        Long userId,

        @NotNull(message = "lectureId 는 필수입니다")
        Long lectureId,

        @NotBlank(message = "잘 이해한 내용을 입력해 주세요")
        String understood,

        @NotBlank(message = "어려웠던 내용을 입력해 주세요")
        String difficult,

        String wantsToLearn
) {
}
