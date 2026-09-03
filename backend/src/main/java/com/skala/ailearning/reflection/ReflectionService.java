package com.skala.ailearning.reflection;

import com.skala.ailearning.ai.*;
import com.skala.ailearning.common.NotFoundException;
import com.skala.ailearning.lecture.Lecture;
import com.skala.ailearning.lecture.LectureMaterial;
import com.skala.ailearning.lecture.LectureMaterialRepository;
import com.skala.ailearning.lecture.LectureRepository;
import com.skala.ailearning.mastery.ConceptMastery;
import com.skala.ailearning.mastery.ConceptMasteryRepository;
import com.skala.ailearning.mastery.MasteryService;
import com.skala.ailearning.quiz.Quiz;
import com.skala.ailearning.quiz.QuizRepository;
import com.skala.ailearning.user.User;
import com.skala.ailearning.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReflectionService {

    private static final int UNDERSTOOD_BONUS = 15;
    private static final int WEAK_PENALTY = 20;

    private final ReflectionRepository reflectionRepository;
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final LectureMaterialRepository materialRepository;
    private final AiAnalysisRepository analysisRepository;
    private final PersonalizedReviewRepository reviewRepository;
    private final QuizRepository quizRepository;
    private final ConceptMasteryRepository masteryRepository;
    private final MasteryService masteryService;
    private final AiAnalysisPort aiAnalysisPort;
    private final ObjectMapper objectMapper;

    public ReflectionService(ReflectionRepository reflectionRepository,
                             UserRepository userRepository,
                             LectureRepository lectureRepository,
                             LectureMaterialRepository materialRepository,
                             AiAnalysisRepository analysisRepository,
                             PersonalizedReviewRepository reviewRepository,
                             QuizRepository quizRepository,
                             ConceptMasteryRepository masteryRepository,
                             MasteryService masteryService,
                             AiAnalysisPort aiAnalysisPort,
                             ObjectMapper objectMapper) {
        this.reflectionRepository = reflectionRepository;
        this.userRepository = userRepository;
        this.lectureRepository = lectureRepository;
        this.materialRepository = materialRepository;
        this.analysisRepository = analysisRepository;
        this.reviewRepository = reviewRepository;
        this.quizRepository = quizRepository;
        this.masteryRepository = masteryRepository;
        this.masteryService = masteryService;
        this.aiAnalysisPort = aiAnalysisPort;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ReflectionResponse create(ReflectionRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다: " + request.userId()));
        Lecture lecture = lectureRepository.findById(request.lectureId())
                .orElseThrow(() -> new NotFoundException("강의를 찾을 수 없습니다: " + request.lectureId()));

        Reflection reflection = reflectionRepository
                .findByUserUserIdAndLectureLectureId(request.userId(), request.lectureId())
                .orElseGet(Reflection::new);

        reflection.setUser(user);
        reflection.setLecture(lecture);
        reflection.setUnderstood(request.understood());
        reflection.setDifficult(request.difficult());
        reflection.setWantsToLearn(request.wantsToLearn());

        return ReflectionResponse.from(reflectionRepository.save(reflection));
    }

    @Transactional
    public AnalysisResponse analyze(Long reflectionId) {
        Reflection reflection = reflectionRepository.findById(reflectionId)
                .orElseThrow(() -> new NotFoundException("회고를 찾을 수 없습니다: " + reflectionId));

        Lecture lecture = reflection.getLecture();
        List<String> materialTitles = materialRepository
                .findByLectureLectureId(lecture.getLectureId()).stream()
                .map(LectureMaterial::getTitle)
                .toList();

        AiAnalysisResult result = aiAnalysisPort.analyze(new AnalysisCommand(
                lecture.getTitle(),
                lecture.getDescription(),
                materialTitles,
                reflection.getUnderstood(),
                reflection.getDifficult(),
                reflection.getWantsToLearn()
        ));

        int levelBefore = masteryService.currentLevel(reflection.getUser().getUserId());

        AiAnalysis analysis = saveAnalysis(reflection, result);
        PersonalizedReview review = saveReview(analysis, result);
        Quiz quiz = saveQuiz(review, result);
        updateMastery(reflection.getUser(), result);

        int levelAfter = masteryService.currentLevel(reflection.getUser().getUserId());

        return toResponse(analysis, review, quiz, result, levelBefore, levelAfter);
    }

    private AiAnalysis saveAnalysis(Reflection reflection, AiAnalysisResult result) {
        AiAnalysis analysis = analysisRepository
                .findByReflectionReflectionId(reflection.getReflectionId())
                .orElseGet(AiAnalysis::new);

        analysis.setReflection(reflection);
        analysis.setUnderstoodTopics(result.understoodTopics());
        analysis.setWeakTopics(result.weakTopics());
        analysis.setRecommendedTopics(result.recommendedTopics());
        analysis.setSummary(result.analysisReason());
        analysis.setUnderstandingScore(result.understandingScore());
        analysis.setUnderstoodSummary(result.understoodSummary());
        analysis.setWeaknessSummary(result.weaknessSummary());
        analysis.setStatus(AnalysisStatus.COMPLETED);

        return analysisRepository.save(analysis);
    }

    private PersonalizedReview saveReview(AiAnalysis analysis, AiAnalysisResult result) {
        PersonalizedReview review = reviewRepository
                .findByAnalysisAnalysisId(analysis.getAnalysisId())
                .orElseGet(PersonalizedReview::new);

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("coreConcepts", result.coreConcepts());
        content.put("exampleCode", result.exampleCode());

        review.setAnalysis(analysis);
        review.setTitle(result.reviewTitle());
        review.setFocusTopics(result.weakTopics());
        review.setContent(writeJson(content));
        review.setStatus(ReviewStatus.NOT_STARTED);

        return reviewRepository.save(review);
    }

    private Quiz saveQuiz(PersonalizedReview review, AiAnalysisResult result) {
        Quiz quiz = quizRepository.findByReviewReviewId(review.getReviewId())
                .orElseGet(Quiz::new);

        quiz.setReview(review);
        quiz.setTitle(result.reviewTitle() + " 확인 문제");
        quiz.setQuestions(writeJson(result.quiz()));

        return quizRepository.save(quiz);
    }

    private void updateMastery(User user, AiAnalysisResult result) {
        result.understoodTopics().forEach(topic ->
                upsertMastery(user, topic, result.understandingScore() + UNDERSTOOD_BONUS));
        result.weakTopics().forEach(topic ->
                upsertMastery(user, topic, result.understandingScore() - WEAK_PENALTY));
    }

    private void upsertMastery(User user, String conceptName, int rawScore) {
        int score = Math.max(0, Math.min(100, rawScore));

        ConceptMastery mastery = masteryRepository
                .findByUserUserIdAndConceptName(user.getUserId(), conceptName)
                .orElseGet(ConceptMastery::new);

        mastery.setUser(user);
        mastery.setConceptName(conceptName);
        mastery.setScore(score);
        mastery.setLevel(ConceptMastery.levelOf(score));

        masteryRepository.save(mastery);
    }

    private AnalysisResponse toResponse(AiAnalysis analysis,
                                        PersonalizedReview review,
                                        Quiz quiz,
                                        AiAnalysisResult result,
                                        int levelBefore,
                                        int levelAfter) {
        List<AnalysisResponse.QuizItem> quizItems = result.quiz().stream()
                .map(item -> new AnalysisResponse.QuizItem(
                        item.conceptName(), item.question(), item.options()))
                .toList();

        return new AnalysisResponse(
                analysis.getAnalysisId(),
                result.understandingScore(),
                result.analysisReason(),
                result.understoodSummary(),
                result.weaknessSummary(),
                result.understoodTopics(),
                result.weakTopics(),
                result.recommendedTopics(),
                levelBefore,
                levelAfter,
                new AnalysisResponse.ReviewMaterial(
                        review.getReviewId(),
                        quiz.getQuizId(),
                        result.reviewTitle(),
                        result.coreConcepts(),
                        result.exampleCode(),
                        quizItems
                )
        );
    }

    private String writeJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }
}
