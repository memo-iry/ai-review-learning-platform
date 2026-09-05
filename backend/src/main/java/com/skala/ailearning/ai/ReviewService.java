package com.skala.ailearning.ai;

import com.skala.ailearning.common.NotFoundException;
import com.skala.ailearning.lecture.Lecture;
import com.skala.ailearning.quiz.Quiz;
import com.skala.ailearning.quiz.QuizAttempt;
import com.skala.ailearning.quiz.QuizAttemptRepository;
import com.skala.ailearning.quiz.QuizRepository;
import com.skala.ailearning.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final PersonalizedReviewRepository reviewRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository attemptRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ReviewService(PersonalizedReviewRepository reviewRepository,
                         QuizRepository quizRepository,
                         QuizAttemptRepository attemptRepository,
                         UserRepository userRepository,
                         ObjectMapper objectMapper) {
        this.reviewRepository = reviewRepository;
        this.quizRepository = quizRepository;
        this.attemptRepository = attemptRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    private record ReviewContent(List<String> coreConcepts, String exampleCode) {
    }

    @Transactional(readOnly = true)
    public List<ReviewSummaryResponse> findAllByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("사용자를 찾을 수 없습니다: " + userId);
        }

        List<PersonalizedReview> reviews = reviewRepository.findAllByUserId(userId);
        if (reviews.isEmpty()) {
            return List.of();
        }

        Map<Long, Quiz> quizByReview = quizRepository
                .findByReviewReviewIdIn(reviews.stream().map(PersonalizedReview::getReviewId).toList())
                .stream()
                .collect(Collectors.toMap(q -> q.getReview().getReviewId(), Function.identity()));

        Map<Long, List<QuizAttempt>> attemptsByQuiz = attemptRepository
                .findByUserUserIdOrderByCompletedAtDesc(userId).stream()
                .collect(Collectors.groupingBy(a -> a.getQuiz().getQuizId()));

        return reviews.stream().map(review -> {
            Quiz quiz = quizByReview.get(review.getReviewId());
            List<QuizAttempt> attempts = quiz == null
                    ? List.of()
                    : attemptsByQuiz.getOrDefault(quiz.getQuizId(), List.of());
            QuizAttempt latest = attempts.stream()
                    .max(Comparator.comparing(QuizAttempt::getAttemptId))
                    .orElse(null);

            AiAnalysis analysis = review.getAnalysis();
            Lecture lecture = analysis.getReflection().getLecture();

            return new ReviewSummaryResponse(
                    review.getReviewId(),
                    quiz == null ? null : quiz.getQuizId(),
                    review.getTitle(),
                    lecture.getLectureId(),
                    lecture.getTitle(),
                    lecture.getLectureDate(),
                    analysis.getUnderstandingScore(),
                    review.getFocusTopics(),
                    review.getStatus().name(),
                    review.getCreatedAt(),
                    latest == null ? null : latest.getScore(),
                    attempts.size());
        }).toList();
    }

    @Transactional(readOnly = true)
    public Long findOwnerUserId(Long reviewId) {
        return reviewRepository.findOwnerUserId(reviewId)
                .orElseThrow(() -> new NotFoundException("복습자료를 찾을 수 없습니다: " + reviewId));
    }

    @Transactional(readOnly = true)
    public ReviewDetailResponse findDetail(Long reviewId) {
        PersonalizedReview review = reviewRepository.findDetailById(reviewId)
                .orElseThrow(() -> new NotFoundException("복습자료를 찾을 수 없습니다: " + reviewId));

        AiAnalysis analysis = review.getAnalysis();
        Lecture lecture = analysis.getReflection().getLecture();
        Long userId = analysis.getReflection().getUser().getUserId();

        ReviewContent content = objectMapper.readValue(review.getContent(), ReviewContent.class);
        Quiz quiz = quizRepository.findByReviewReviewId(reviewId).orElse(null);

        List<AnalysisResponse.QuizItem> quizItems = List.of();
        List<ReviewDetailResponse.Attempt> attempts = List.of();

        if (quiz != null) {
            List<AiAnalysisResult.QuizItem> stored =
                    objectMapper.readValue(quiz.getQuestions(), new TypeReference<>() {
                    });
            quizItems = stored.stream()
                    .map(item -> new AnalysisResponse.QuizItem(
                            item.conceptName(), item.question(), item.options()))
                    .toList();

            attempts = attemptRepository
                    .findByQuizQuizIdAndUserUserId(quiz.getQuizId(), userId).stream()
                    .map(a -> new ReviewDetailResponse.Attempt(
                            a.getAttemptId(), a.getScore(),
                            a.getCorrectCount(), a.getTotalCount(), a.getCompletedAt()))
                    .toList();
        }

        return new ReviewDetailResponse(
                review.getReviewId(),
                quiz == null ? null : quiz.getQuizId(),
                review.getTitle(),
                lecture.getLectureId(),
                lecture.getTitle(),
                lecture.getLectureDate(),
                analysis.getUnderstandingScore(),
                analysis.getSummary(),
                analysis.getUnderstoodSummary(),
                analysis.getWeaknessSummary(),
                analysis.getUnderstoodTopics(),
                analysis.getWeakTopics(),
                content.coreConcepts(),
                content.exampleCode(),
                quizItems,
                review.getStatus().name(),
                review.getCreatedAt(),
                attempts);
    }
}
