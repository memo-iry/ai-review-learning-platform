package com.g129.ailearning.reflection;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReflectionRepository extends JpaRepository<Reflection, Long> {
    List<Reflection> findByLearnerIdOrderByCreatedAtDesc(Long learnerId);
}

