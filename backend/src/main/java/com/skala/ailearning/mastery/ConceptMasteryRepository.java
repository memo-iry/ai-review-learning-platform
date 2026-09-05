package com.skala.ailearning.mastery;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConceptMasteryRepository extends JpaRepository<ConceptMastery, Long> {
    List<ConceptMastery> findByUserUserIdOrderByScoreAsc(Long userId);

    Optional<ConceptMastery> findByUserUserIdAndConceptName(Long userId, String conceptName);
}
