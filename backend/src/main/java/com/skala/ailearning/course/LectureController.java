package com.skala.ailearning.course;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/lectures")
@Tag(name = "강의", description = "일자별 강의 조회 API")
public class LectureController {
    private final LectureRepository lectureRepository;
    public LectureController(LectureRepository lectureRepository) { this.lectureRepository = lectureRepository; }

    @GetMapping
    @Operation(summary = "강의 목록 조회")
    public List<LectureSummary> findAll() {
        return lectureRepository.findAllByOrderByLectureDateDescStartTimeAsc().stream()
                .map(lecture -> new LectureSummary(lecture.getId(), lecture.getTitle(), lecture.getDescription(),
                        lecture.getLectureDate(), lecture.getStartTime(), lecture.getEndTime()))
                .toList();
    }

    public record LectureSummary(Long lectureId, String title, String description, LocalDate lectureDate,
                                 LocalTime startTime, LocalTime endTime) {}
}
