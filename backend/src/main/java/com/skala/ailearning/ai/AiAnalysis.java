package com.skala.ailearning.ai;

import com.skala.ailearning.reflection.Reflection;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(
        name = "ai_analyses",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_analysis_reflection",
                columnNames = "reflection_id"
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long analysisId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reflection_id", nullable = false, unique = true)
    private Reflection reflection;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "understood_topics")
    private List<String> understoodTopics;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "weak_topics")
    private List<String> weakTopics;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recommended_topics")
    private List<String> recommendedTopics;

    @Column(columnDefinition = "text")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnalysisStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
