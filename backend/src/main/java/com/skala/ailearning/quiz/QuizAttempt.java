package com.skala.ailearning.quiz;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.skala.ailearning.user.User;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id") private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false) private Quiz quiz;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false) private User user;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "json") private String answers;
    private Integer score;
    @Column(name = "correct_count", nullable = false) private int correctCount;
    @Column(name = "total_count", nullable = false) private int totalCount;
    @Column(name = "completed_at") private LocalDateTime completedAt;
    protected QuizAttempt() {}
}
