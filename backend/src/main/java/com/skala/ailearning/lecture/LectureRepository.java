package com.skala.ailearning.lecture;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    List<Lecture> findAllByOrderByLectureDateDesc();

    List<Lecture> findByLectureDate(LocalDate lectureDate);

    List<Lecture> findByTitleContainingIgnoreCase(String keyword);
}
