package com.g129.ailearning.course;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/courses")
@Tag(name = "교육과정", description = "교육과정 조회 API")
public class CourseController {

    private final CourseRepository courseRepository;

    public CourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @GetMapping
    @Operation(summary = "교육과정 목록 조회")
    public List<CourseSummary> findAll() {
        return courseRepository.findAll().stream()
                .map(course -> new CourseSummary(course.getId(), course.getName(), course.getDescription()))
                .toList();
    }

    public record CourseSummary(Long courseId, String courseName, String description) {
    }
}
