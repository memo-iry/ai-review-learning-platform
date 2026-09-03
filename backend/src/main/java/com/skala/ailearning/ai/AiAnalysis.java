package com.skala.ailearning.ai;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.skala.ailearning.reflection.Reflection;

@Entity
@Table(name = "ai_analyses", uniqueConstraints = @UniqueConstraint(name = "uk_analysis_reflection", columnNames = "reflection_id"))
public class AiAnalysis {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id") private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reflection_id", nullable = false) private Reflection reflection;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "understood_topics", columnDefinition = "json") private String understoodTopics;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "weak_topics", columnDefinition = "json") private String weakTopics;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "recommended_topics", columnDefinition = "json") private String recommendedTopics;
    private String summary;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private AnalysisStatus status;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    protected AiAnalysis() {}
    public AiAnalysis(Reflection reflection, String understoodTopics, String weakTopics,
                      String recommendedTopics, String summary, AnalysisStatus status) {
        this.reflection = reflection;
        this.understoodTopics = understoodTopics;
        this.weakTopics = weakTopics;
        this.recommendedTopics = recommendedTopics;
        this.summary = summary;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
}
