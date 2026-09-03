package com.g129.ailearning.course;

import java.time.LocalDateTime;

import com.g129.ailearning.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private User instructor;

    @Column(name = "course_name", nullable = false, length = 100)
    private String name;

    @Column(name = "course_description")
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Course() {
    }

    public Long getId() {
        return id;
    }

    public User getInstructor() {
        return instructor;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}

