package com.skala.ailearning.material;

import java.time.LocalDateTime;

import com.skala.ailearning.course.Lecture;

import jakarta.persistence.*;

@Entity
@Table(name = "lecture_materials")
public class LectureMaterial {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;
    @Column(nullable = false, length = 255)
    private String title;
    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false, length = 20)
    private MaterialType materialType;
    @Column(name = "file_url", length = 1000)
    private String fileUrl;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected LectureMaterial() {}
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public MaterialType getMaterialType() { return materialType; }
    public String getFileUrl() { return fileUrl; }
}
