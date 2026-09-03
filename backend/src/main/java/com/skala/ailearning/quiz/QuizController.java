package com.skala.ailearning.quiz;

import com.skala.ailearning.common.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Quiz", description = "확인 문제 응시와 채점")
@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

        private final QuizService quizService;
        private final com.skala.ailearning.common.AccessGuard accessGuard;

        public QuizController(QuizService quizService,
                        com.skala.ailearning.common.AccessGuard accessGuard) {
                this.quizService = quizService;
                this.accessGuard = accessGuard;
        }

        @Operation(summary = "Quiz 단건 조회", description = """
                        Quiz 문항을 조회한다.
                        아직 응시 전이므로 정답(answerIndex)과 해설(explanation)은 내려주지 않는다.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공"),
                        @ApiResponse(responseCode = "404", description = "Quiz 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping("/{quizId}")
        public QuizResponse get(@PathVariable Long quizId) {
                accessGuard.requireSelfOrAdmin(quizService.findOwnerUserId(quizId));
                return quizService.getQuiz(quizId);
        }

        @Operation(summary = "Quiz 전체 목록 조회", description = """
                        특정 사용자와 연결된 Quiz를 모두 조회한다.
                        회고 → 복습자료 → Quiz 로 이어지는 사슬을 따라, 그 사용자의 회고에서 생성된 Quiz만 대상이 된다.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공"),
                        @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping
        public List<QuizResponse> list(@RequestParam Long userId) {
                accessGuard.requireSelfOrAdmin(userId);
                return quizService.findAllByUser(userId);
        }

        @Operation(summary = "Quiz 응시 이력 조회", description = """
                        특정 사용자의 Quiz 응시 이력을 완료 시각 최신순으로 조회한다.
                        지난 퀴즈 목록에서 정답률을 보여줄 때 사용한다.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공"),
                        @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping("/attempts")
        public List<QuizAttemptSummaryResponse> attempts(@RequestParam Long userId) {
                accessGuard.requireSelfOrAdmin(userId);
                return quizService.findAttemptsByUser(userId);
        }

        @Operation(summary = "Quiz 응시", description = """
                        선택한 답안을 채점하고 결과를 저장한다.
                        answers 는 문항 순서대로의 보기 인덱스(0부터)다.

                        채점 결과는 개념별 이해도에 반영된다.
                        맞으면 해당 개념 점수가 오르고 틀리면 내려간다.
                        회고 → 복습 → Quiz → 이해도 갱신으로 학습 사이클이 닫히는 지점이다.

                        정답과 해설은 응시 전에는 내려주지 않는다. 채점 응답에만 포함된다.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "채점 완료"),
                        @ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Quiz 또는 사용자 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PostMapping("/{quizId}/attempts")
        @ResponseStatus(HttpStatus.CREATED)
        public QuizAttemptResponse submit(@PathVariable Long quizId,
                        @Valid @RequestBody QuizAttemptRequest request) {
                accessGuard.requireSelfOrAdmin(request.userId());
                return quizService.submit(quizId, request);
        }
}