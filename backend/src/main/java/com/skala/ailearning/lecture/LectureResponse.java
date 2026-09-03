package com.skala.ailearning.lecture;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record LectureResponse(
        Long lectureId,
        String title,
        String description,
        LocalDate lectureDate,
        String startTime,
        String endTime
) {
    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    public static LectureResponse from(Lecture lecture) {
        return new LectureResponse(
                lecture.getLectureId(),
                lecture.getTitle(),
                lecture.getDescription(),
                lecture.getLectureDate(),
                format(lecture.getStartTime()),
                format(lecture.getEndTime())
        );
    }

    private static String format(LocalTime time) {
        return time == null ? null : time.format(HH_MM);
    }
}
