package com.skala.ailearning.mastery;

import com.skala.ailearning.common.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "학습 수준", description = "개념별 이해도와 학습 진도")
@RestController
@RequestMapping("/api/users")
public class MasteryController {

    private final MasteryService masteryService;
    private final com.skala.ailearning.common.AccessGuard accessGuard;

    public MasteryController(MasteryService masteryService,
                             com.skala.ailearning.common.AccessGuard accessGuard) {
        this.masteryService = masteryService;
        this.accessGuard = accessGuard;
    }

    @Operation(
            summary = "이해도 조회",
            description = """
                    대시보드에 표시할 현재 학습 수준을 반환한다.

                    currentLevel  개념별 점수 평균에서 파생된 Learning Level (1 인지 · 2 이해 · 3 적용 · 4 구현)
                    averageScore  개념별 이해도 점수 평균
                    progressRate  회고를 작성한 강의 수 / 전체 강의 수
                    weakest       점수가 낮은 개념 상위 3개

                    값은 회고 분석과 Quiz 결과가 누적되며 갱신된다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "다른 사용자의 데이터",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{userId}/mastery")
    public MasteryResponse getMastery(@PathVariable Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        return masteryService.getMastery(userId);
    }
}
