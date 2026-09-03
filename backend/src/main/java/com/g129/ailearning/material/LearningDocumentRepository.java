package com.g129.ailearning.material;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningDocumentRepository extends JpaRepository<LearningDocument, Long> {
    List<LearningDocument> findByCourseIdOrderByIdAsc(Long courseId);
}

