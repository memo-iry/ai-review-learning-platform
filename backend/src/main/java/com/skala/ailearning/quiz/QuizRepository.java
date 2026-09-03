package com.skala.ailearning.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Optional<Quiz> findByReviewReviewId(Long reviewId);

    List<Quiz> findByReviewReviewIdIn(Collection<Long> reviewIds);

    @Query("""
            SELECT q FROM Quiz q
            WHERE q.review.analysis.reflection.user.userId = :userId
            ORDER BY q.createdAt DESC
            """)
    List<Quiz> findAllByUserId(@Param("userId") Long userId);
}