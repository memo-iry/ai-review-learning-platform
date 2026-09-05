package com.skala.ailearning.quiz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserUserIdOrderByCompletedAtDesc(Long userId);

    List<QuizAttempt> findByQuizQuizIdAndUserUserId(Long quizId, Long userId);
}
