package com.skala.ailearning.reflection;

import java.time.OffsetDateTime;

public record ReflectionResponse(
        Long reflectionId,
        Long userId,
        Long lectureId,
        String understood,
        String difficult,
        String wantsToLearn,
        OffsetDateTime createdAt
) {
    public static ReflectionResponse from(Reflection reflection) {
        return new ReflectionResponse(
                reflection.getReflectionId(),
                reflection.getUser().getUserId(),
                reflection.getLecture().getLectureId(),
                reflection.getUnderstood(),
                reflection.getDifficult(),
                reflection.getWantsToLearn(),
                reflection.getCreatedAt()
        );
    }
}
