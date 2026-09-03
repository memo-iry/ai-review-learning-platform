package com.skala.ailearning.reflection;

import java.time.LocalDateTime;

public record ReflectionResponse(
        Long reflectionId,
        Long userId,
        Long lectureId,
        String understood,
        String difficult,
        String wantsToLearn,
        LocalDateTime createdAt) {

    public static ReflectionResponse from(Reflection reflection) {
        return new ReflectionResponse(
                reflection.getId(),
                reflection.getLearner().getId(),
                reflection.getLecture().getId(),
                reflection.getUnderstood(),
                reflection.getDifficult(),
                reflection.getWantsToLearn(),
                reflection.getCreatedAt());
    }
}
