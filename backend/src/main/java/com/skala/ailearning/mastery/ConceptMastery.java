package com.skala.ailearning.mastery;

import java.time.LocalDateTime;

import com.skala.ailearning.user.User;

import jakarta.persistence.*;

@Entity
@Table(name = "concept_mastery", uniqueConstraints = @UniqueConstraint(name = "uk_user_concept", columnNames = {"user_id", "concept_name"}))
public class ConceptMastery {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mastery_id") private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(name = "concept_name", nullable = false, length = 150) private String conceptName;
    @Column(nullable = false) private int level;
    private Integer score;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    protected ConceptMastery() {}
}
