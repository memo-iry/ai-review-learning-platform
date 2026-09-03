package com.g129.ailearning.reflection;

import java.time.LocalDateTime;

public record ReflectionResponse(
        Long reflectionId,
        Long userId,
        Long courseId,
        String understoodContent,
        String difficultContent,
        String questionContent,
        ReflectionStatus status,
        LocalDateTime createdAt) {

    public static ReflectionResponse from(Reflection reflection) {
        return new ReflectionResponse(
                reflection.getId(),
                reflection.getLearner().getId(),
                reflection.getCourse().getId(),
                reflection.getUnderstoodContent(),
                reflection.getDifficultContent(),
                reflection.getQuestionContent(),
                reflection.getStatus(),
                reflection.getCreatedAt());
    }
}

