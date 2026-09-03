package com.skala.ailearning.reflection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReflectionRepository extends JpaRepository<Reflection, Long> {

    Optional<Reflection> findByUserUserIdAndLectureLectureId(Long userId, Long lectureId);

    List<Reflection> findByUserUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserUserIdAndLectureLectureId(Long userId, Long lectureId);
}
