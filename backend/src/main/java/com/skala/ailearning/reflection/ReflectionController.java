package com.skala.ailearning.reflection;

import com.skala.ailearning.ai.AnalysisResponse;
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

@Tag(name = "학습 회고", description = "회고 작성과 AI 이해도 분석")
@RestController
@RequestMapping("/api/reflections")
public class ReflectionController {

    private final ReflectionService reflectionService;

    public ReflectionController(ReflectionService reflectionService) {
        this.reflectionService = reflectionService;
    }

    @Operation(
            summary = "회고 저장",
            description = """
                    수업 후 작성한 회고를 저장한다.
                    한 사용자가 한 강의에 남기는 회고는 하나다. 같은 강의에 다시 제출하면 새로 쌓지 않고 갱신한다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 또는 강의 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReflectionResponse create(@Valid @RequestBody ReflectionRequest request) {
        return reflectionService.create(request);
    }

    @Operation(
            summary = "이해도 분석 및 복습자료 생성",
            description = """
                    강의자료와 회고를 함께 분석해 이해한 개념과 취약 개념을 나누고,
                    취약 개념 중심의 복습자료와 확인 문제를 생성한다.

                    AI 확장 지점 세 곳이 이 호출 하나에서 동작한다.
                    Reflection Analyzer → Review Generator → Quiz Generator

                    현재는 MockAiAnalysisAdapter 가 응답한다. 회고 본문에서 개념을 찾아내므로
                    입력이 달라지면 결과도 달라진다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "분석 성공"),
            @ApiResponse(responseCode = "404", description = "회고 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{reflectionId}/analyze")
    public AnalysisResponse analyze(@PathVariable Long reflectionId) {
        return reflectionService.analyze(reflectionId);
    }
}
