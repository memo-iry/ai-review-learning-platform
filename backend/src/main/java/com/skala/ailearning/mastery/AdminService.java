package com.skala.ailearning.mastery;

import com.skala.ailearning.reflection.ReflectionRepository;
import com.skala.ailearning.user.UserRepository;
import com.skala.ailearning.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private static final int WEAKEST_LIMIT = 5;

    private final ConceptMasteryRepository masteryRepository;
    private final ReflectionRepository reflectionRepository;
    private final UserRepository userRepository;

    public AdminService(ConceptMasteryRepository masteryRepository,
                        ReflectionRepository reflectionRepository,
                        UserRepository userRepository) {
        this.masteryRepository = masteryRepository;
        this.reflectionRepository = reflectionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public AdminOverviewResponse overview() {
        Map<String, List<ConceptMastery>> byConcept = masteryRepository.findAll().stream()
                .filter(mastery -> mastery.getScore() != null)
                .collect(Collectors.groupingBy(ConceptMastery::getConceptName));

        List<AdminOverviewResponse.ConceptStat> concepts = byConcept.entrySet().stream()
                .map(entry -> {
                    int average = (int) entry.getValue().stream()
                            .mapToInt(ConceptMastery::getScore)
                            .average()
                            .orElse(0);
                    return new AdminOverviewResponse.ConceptStat(
                            entry.getKey(),
                            average,
                            ConceptMastery.levelOf(average),
                            entry.getValue().size());
                })
                .sorted(Comparator.comparingInt(AdminOverviewResponse.ConceptStat::averageScore))
                .toList();

        long learnerCount = userRepository.findAll().stream()
                .filter(user -> user.getRole() == UserRole.LEARNER)
                .count();

        return new AdminOverviewResponse(
                (int) learnerCount,
                (int) reflectionRepository.count(),
                concepts.size(),
                concepts,
                concepts.stream().limit(WEAKEST_LIMIT).toList());
    }
}
