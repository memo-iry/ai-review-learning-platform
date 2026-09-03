package com.skala.ailearning.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonalizedReviewRepository extends JpaRepository<PersonalizedReview, Long> {

    Optional<PersonalizedReview> findByAnalysisAnalysisId(Long analysisId);

    List<PersonalizedReview> findByAnalysisReflectionUserUserIdOrderByCreatedAtDesc(Long userId);
}
