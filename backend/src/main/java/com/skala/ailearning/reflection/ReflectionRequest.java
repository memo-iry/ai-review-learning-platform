package com.skala.ailearning.reflection;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReflectionRequest(
        @NotNull Long userId,
        @NotNull Long lectureId,
        @NotBlank String understood,
        @NotBlank String difficult,
        String wantsToLearn) {
}
