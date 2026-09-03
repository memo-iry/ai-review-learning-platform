package com.skala.ailearning.reflection;

import java.time.LocalDateTime;

import com.skala.ailearning.course.Lecture;
import com.skala.ailearning.user.User;

import jakarta.persistence.*;

@Entity
@Table(name = "reflections", uniqueConstraints = @UniqueConstraint(name = "uk_reflection_user_lecture", columnNames = {"user_id", "lecture_id"}))
public class Reflection {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reflection_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User learner;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;
    private String understood;
    private String difficult;
    @Column(name = "wants_to_learn")
    private String wantsToLearn;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Reflection() {}
    public Reflection(User learner, Lecture lecture, String understood, String difficult, String wantsToLearn) {
        this.learner = learner; this.lecture = lecture; this.understood = understood;
        this.difficult = difficult; this.wantsToLearn = wantsToLearn; this.createdAt = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public User getLearner() { return learner; }
    public Lecture getLecture() { return lecture; }
    public String getUnderstood() { return understood; }
    public String getDifficult() { return difficult; }
    public String getWantsToLearn() { return wantsToLearn; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
