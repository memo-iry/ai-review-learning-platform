package com.skala.ailearning.lecture;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureMaterialRepository extends JpaRepository<LectureMaterial, Long> {
    List<LectureMaterial> findByLectureLectureId(Long lectureId);
}
