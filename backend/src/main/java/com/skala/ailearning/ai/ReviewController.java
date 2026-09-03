package com.skala.ailearning.ai;

import com.skala.ailearning.common.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "복습자료", description = "지난 복습자료와 Quiz 조회")
@RestController
public class ReviewController {

    private final ReviewService reviewService;
    private final com.skala.ailearning.common.AccessGuard accessGuard;

    public ReviewController(ReviewService reviewService,
                            com.skala.ailearning.common.AccessGuard accessGuard) {
        this.reviewService = reviewService;
        this.accessGuard = accessGuard;
    }

    @Operation(
            summary = "내 복습자료 목록",
            description = """
                    지금까지 생성된 복습자료를 최신순으로 반환한다.
                    강의명, 이해도 점수, 복습 대상 개념, 마지막 Quiz 점수와 응시 횟수를 함께 준다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/users/{userId}/reviews")
    public List<ReviewSummaryResponse> getReviews(@PathVariable Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        return reviewService.findAllByUser(userId);
    }

    @Operation(
            summary = "복습자료 상세",
            description = """
                    핵심 개념, 예제 코드, 확인 문제와 지난 응시 기록을 함께 반환한다.
                    확인 문제의 정답과 해설은 포함하지 않는다. 채점 응답에만 내려간다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "복습자료 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/reviews/{reviewId}")
    public ReviewDetailResponse getReview(@PathVariable Long reviewId) {
        accessGuard.requireSelfOrAdmin(reviewService.findOwnerUserId(reviewId));
        return reviewService.findDetail(reviewId);
    }
}
