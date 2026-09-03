package com.skala.ailearning.quiz;

import com.skala.ailearning.ai.AiAnalysisResult;
import com.skala.ailearning.common.NotFoundException;
import com.skala.ailearning.mastery.ConceptMastery;
import com.skala.ailearning.mastery.ConceptMasteryRepository;
import com.skala.ailearning.mastery.MasteryService;
import com.skala.ailearning.user.User;
import com.skala.ailearning.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuizService {

    private static final int CORRECT_GAIN = 12;
    private static final int WRONG_LOSS = 8;

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository attemptRepository;
    private final UserRepository userRepository;
    private final ConceptMasteryRepository masteryRepository;
    private final MasteryService masteryService;
    private final ObjectMapper objectMapper;

    public QuizService(QuizRepository quizRepository,
            QuizAttemptRepository attemptRepository,
            UserRepository userRepository,
            ConceptMasteryRepository masteryRepository,
            MasteryService masteryService,
            ObjectMapper objectMapper) {
        this.quizRepository = quizRepository;
        this.attemptRepository = attemptRepository;
        this.userRepository = userRepository;
        this.masteryRepository = masteryRepository;
        this.masteryService = masteryService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public QuizResponse getQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz 를 찾을 수 없습니다: " + quizId));

        return toResponse(quiz);
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> findAllByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다: " + userId));

        return quizRepository.findAllByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Long findOwnerUserId(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz 를 찾을 수 없습니다: " + quizId));

        return quiz.getReview().getAnalysis().getReflection().getUser().getUserId();
    }

    private QuizResponse toResponse(Quiz quiz) {
        List<QuizResponse.QuestionItem> questions = readQuestions(quiz).stream()
                .map(item -> new QuizResponse.QuestionItem(
                        item.conceptName(), item.question(), item.options()))
                .toList();

        Long lectureId = quiz.getReview().getAnalysis().getReflection().getLecture().getLectureId();

        return new QuizResponse(quiz.getQuizId(), lectureId, quiz.getTitle(), quiz.getCreatedAt(), questions);
    }

    @Transactional(readOnly = true)
    public List<QuizAttemptSummaryResponse> findAttemptsByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다: " + userId));

        return attemptRepository.findByUserUserIdOrderByCompletedAtDesc(userId).stream()
                .map(this::toSummary)
                .toList();
    }

    private QuizAttemptSummaryResponse toSummary(QuizAttempt attempt) {
        Quiz quiz = attempt.getQuiz();

        return new QuizAttemptSummaryResponse(
                attempt.getAttemptId(),
                quiz.getQuizId(),
                quiz.getTitle(),
                quiz.getReview().getAnalysis().getReflection().getLecture().getLectureId(),
                attempt.getScore(),
                attempt.getCorrectCount(),
                attempt.getTotalCount(),
                attempt.getCompletedAt());
    }

    @Transactional
    public QuizAttemptResponse submit(Long quizId, QuizAttemptRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz 를 찾을 수 없습니다: " + quizId));
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다: " + request.userId()));

        List<AiAnalysisResult.QuizItem> items = readQuestions(quiz);
        int levelBefore = masteryService.currentLevel(user.getUserId());

        List<QuizAttemptResponse.Graded> graded = new ArrayList<>();
        List<QuizAttemptResponse.MasteryChange> changes = new ArrayList<>();
        int correctCount = 0;

        for (int i = 0; i < items.size(); i++) {
            AiAnalysisResult.QuizItem item = items.get(i);
            int selected = i < request.answers().size() ? request.answers().get(i) : -1;
            boolean correct = selected == item.answerIndex();

            if (correct) {
                correctCount++;
            }

            graded.add(new QuizAttemptResponse.Graded(
                    item.conceptName(), item.question(), item.options(),
                    selected, item.answerIndex(), correct, item.explanation()));

            changes.add(applyMastery(user, item.conceptName(), correct));
        }

        int total = items.size();
        int score = total == 0 ? 0 : correctCount * 100 / total;

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .user(user)
                .answers(writeJson(request.answers()))
                .score(score)
                .correctCount(correctCount)
                .totalCount(total)
                .completedAt(OffsetDateTime.now())
                .build();

        attempt = attemptRepository.save(attempt);

        return new QuizAttemptResponse(
                attempt.getAttemptId(),
                score,
                correctCount,
                total,
                graded,
                changes,
                levelBefore,
                masteryService.currentLevel(user.getUserId()));
    }

    private QuizAttemptResponse.MasteryChange applyMastery(User user, String conceptName, boolean correct) {
        ConceptMastery mastery = masteryRepository
                .findByUserUserIdAndConceptName(user.getUserId(), conceptName)
                .orElseGet(ConceptMastery::new);

        int before = mastery.getScore() == null ? 0 : mastery.getScore();
        int after = Math.max(0, Math.min(100, before + (correct ? CORRECT_GAIN : -WRONG_LOSS)));

        mastery.setUser(user);
        mastery.setConceptName(conceptName);
        mastery.setScore(after);
        mastery.setLevel(ConceptMastery.levelOf(after));
        masteryRepository.save(mastery);

        return new QuizAttemptResponse.MasteryChange(
                conceptName, before, after,
                ConceptMastery.levelOf(before), ConceptMastery.levelOf(after));
    }

    private List<AiAnalysisResult.QuizItem> readQuestions(Quiz quiz) {
        return objectMapper.readValue(quiz.getQuestions(), new TypeReference<>() {
        });
    }

    private String writeJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }
}