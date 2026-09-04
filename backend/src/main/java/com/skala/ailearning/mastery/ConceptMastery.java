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

    /**
     * 분석기가 만든 개념별 상태 서술. 복습자료 생성 프롬프트에 실어 보낼 값이다.
     * 파생 값이며 원본이 아니다. 갱신될 때마다 덮어쓰이므로 이력은 ai_analyses 와 reflections 에 남는다.
     */
    @Column(name = "summary", columnDefinition = "text")
    private String summary;

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
