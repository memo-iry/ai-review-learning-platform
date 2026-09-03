package com.skala.ailearning.mastery;

import com.skala.ailearning.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "concept_mastery",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_concept",
                columnNames = {"user_id", "concept_name"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConceptMastery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mastery_id")
    private Long masteryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "concept_name", nullable = false, length = 150)
    private String conceptName;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "score")
    private Integer score;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = OffsetDateTime.now();
    }

    public static int levelOf(int score) {
        if (score <= 25) return 1;
        if (score <= 50) return 2;
        if (score <= 80) return 3;
        return 4;
    }
}
