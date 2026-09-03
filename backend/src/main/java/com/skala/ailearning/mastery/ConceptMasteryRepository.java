package com.skala.ailearning.mastery;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ConceptMasteryRepository extends JpaRepository<ConceptMastery, Long> {
    List<ConceptMastery> findByUserIdOrderByConceptName(Long userId);
}
