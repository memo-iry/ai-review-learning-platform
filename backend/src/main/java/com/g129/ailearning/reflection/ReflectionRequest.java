package com.g129.ailearning.reflection;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReflectionRequest(
        @NotNull Long userId,
        @NotNull Long courseId,
        @NotBlank String understoodContent,
        @NotBlank String difficultContent,
        String questionContent) {
}

