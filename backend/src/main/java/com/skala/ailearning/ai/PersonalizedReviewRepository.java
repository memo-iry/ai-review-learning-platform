package com.skala.ailearning.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PersonalizedReviewRepository extends JpaRepository<PersonalizedReview, Long> {

    Optional<PersonalizedReview> findByAnalysisAnalysisId(Long analysisId);

    @Query("""
            select r from PersonalizedReview r
            join fetch r.analysis a
            join fetch a.reflection f
            join fetch f.lecture
            where f.user.userId = :userId
            order by r.createdAt desc
            """)
    List<PersonalizedReview> findAllByUserId(@Param("userId") Long userId);

    @Query("""
            select r from PersonalizedReview r
            join fetch r.analysis a
            join fetch a.reflection f
            join fetch f.lecture
            join fetch f.user
            where r.reviewId = :reviewId
            """)
    Optional<PersonalizedReview> findDetailById(@Param("reviewId") Long reviewId);
}
