package com.skala.ailearning.material;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureMaterialRepository extends JpaRepository<LectureMaterial, Long> {
    List<LectureMaterial> findByLectureIdOrderByIdAsc(Long lectureId);
}
