package com.skala.ailearning.quiz;

import com.skala.ailearning.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "quiz_attempts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Long attemptId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answers")
    private String answers;

    @Column(name = "score")
    private Integer score;

    @Builder.Default
    @Column(name = "correct_count", nullable = false)
    private Integer correctCount = 0;

    @Builder.Default
    @Column(name = "total_count", nullable = false)
    private Integer totalCount = 0;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;
}
