package com.skala.ailearning.ai;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "personalized_reviews", uniqueConstraints = @UniqueConstraint(name = "uk_review_analysis", columnNames = "analysis_id"))
public class PersonalizedReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id") private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false) private AiAnalysis analysis;
    @Column(nullable = false, length = 255) private String title;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "focus_topics", columnDefinition = "json") private String focusTopics;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "json") private String content;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ReviewStatus status;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "completed_at") private LocalDateTime completedAt;
    protected PersonalizedReview() {}
    public PersonalizedReview(AiAnalysis analysis, String title, String focusTopics, String content) {
        this.analysis = analysis;
        this.title = title;
        this.focusTopics = focusTopics;
        this.content = content;
        this.status = ReviewStatus.NOT_STARTED;
        this.createdAt = LocalDateTime.now();
    }
}
