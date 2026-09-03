package com.skala.ailearning.quiz;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.skala.ailearning.ai.PersonalizedReview;

@Entity
@Table(name = "quizzes", uniqueConstraints = @UniqueConstraint(name = "uk_quiz_review", columnNames = "review_id"))
public class Quiz {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id") private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false) private PersonalizedReview review;
    @Column(nullable = false, length = 255) private String title;
    @JdbcTypeCode(SqlTypes.JSON) @Column(nullable = false, columnDefinition = "json") private String questions;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    protected Quiz() {}
    public Quiz(PersonalizedReview review, String title, String questions) {
        this.review = review;
        this.title = title;
        this.questions = questions;
        this.createdAt = LocalDateTime.now();
    }
}
