package com.g129.ailearning.reflection;

import java.time.LocalDateTime;

import com.g129.ailearning.course.Course;
import com.g129.ailearning.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reflections")
public class Reflection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reflections_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "users_id", nullable = false)
    private User learner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "understood_content", nullable = false)
    private String understoodContent;

    @Column(name = "difficult_content", nullable = false)
    private String difficultContent;

    @Column(name = "question_content")
    private String questionContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "reflection_status", nullable = false, length = 20)
    private ReflectionStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Reflection() {
    }

    public Reflection(User learner, Course course, String understoodContent,
                      String difficultContent, String questionContent) {
        this.learner = learner;
        this.course = course;
        this.understoodContent = understoodContent;
        this.difficultContent = difficultContent;
        this.questionContent = questionContent;
        this.status = ReflectionStatus.SUBMITTED;
        this.createdAt = LocalDateTime.now();
    }

    public void markAnalyzed() {
        this.status = ReflectionStatus.ANALYZED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markNeedsRevision() {
        this.status = ReflectionStatus.NEEDS_REVISION;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getLearner() {
        return learner;
    }

    public Course getCourse() {
        return course;
    }

    public String getUnderstoodContent() {
        return understoodContent;
    }

    public String getDifficultContent() {
        return difficultContent;
    }

    public String getQuestionContent() {
        return questionContent;
    }

    public ReflectionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

