package com.skala.ailearning.reflection;

import com.skala.ailearning.lecture.Lecture;
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
        name = "reflections",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reflection_user_lecture",
                columnNames = {"user_id", "lecture_id"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reflection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reflection_id")
    private Long reflectionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @Column(columnDefinition = "text")
    private String understood;

    @Column(columnDefinition = "text")
    private String difficult;

    @Column(name = "wants_to_learn", columnDefinition = "text")
    private String wantsToLearn;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
