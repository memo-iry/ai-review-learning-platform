package com.skala.ailearning.mastery;

import com.skala.ailearning.common.NotFoundException;
import com.skala.ailearning.lecture.LectureRepository;
import com.skala.ailearning.reflection.ReflectionRepository;
import com.skala.ailearning.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MasteryService {

    private static final int WEAKEST_LIMIT = 3;

    private final ConceptMasteryRepository masteryRepository;
    private final ReflectionRepository reflectionRepository;
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;

    public MasteryService(ConceptMasteryRepository masteryRepository,
                          ReflectionRepository reflectionRepository,
                          LectureRepository lectureRepository,
                          UserRepository userRepository) {
        this.masteryRepository = masteryRepository;
        this.reflectionRepository = reflectionRepository;
        this.lectureRepository = lectureRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public MasteryResponse getMastery(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("사용자를 찾을 수 없습니다: " + userId);
        }

        List<ConceptMastery> masteries = masteryRepository.findByUserUserIdOrderByScoreAsc(userId);

        int averageScore = averageScore(masteries);
        int reviewedLectures = reflectionRepository.findByUserUserIdOrderByCreatedAtDesc(userId).size();
        int totalLectures = (int) lectureRepository.count();

        List<MasteryResponse.ConceptScore> concepts = masteries.stream()
                .map(MasteryResponse.ConceptScore::from)
                .toList();

        return new MasteryResponse(
                userId,
                ConceptMastery.levelOf(averageScore),
                averageScore,
                totalLectures == 0 ? 0 : reviewedLectures * 100 / totalLectures,
                reviewedLectures,
                totalLectures,
                masteries.size(),
                concepts,
                concepts.stream().limit(WEAKEST_LIMIT).toList()
        );
    }

    @Transactional(readOnly = true)
    public int currentLevel(Long userId) {
        return ConceptMastery.levelOf(
                averageScore(masteryRepository.findByUserUserIdOrderByScoreAsc(userId)));
    }

    private int averageScore(List<ConceptMastery> masteries) {
        return (int) masteries.stream()
                .filter(mastery -> mastery.getScore() != null)
                .mapToInt(ConceptMastery::getScore)
                .average()
                .orElse(0);
    }
}
