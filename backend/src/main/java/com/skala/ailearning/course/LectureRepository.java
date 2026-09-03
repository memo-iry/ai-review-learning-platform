package com.skala.ailearning.course;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    List<Lecture> findAllByOrderByLectureDateDescStartTimeAsc();
}
