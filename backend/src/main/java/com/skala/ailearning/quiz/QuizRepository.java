package com.skala.ailearning.quiz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Optional<Quiz> findByReviewReviewId(Long reviewId);

    List<Quiz> findByReviewReviewIdIn(Collection<Long> reviewIds);
}
