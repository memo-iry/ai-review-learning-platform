package com.skala.ailearning.lecture;

import com.skala.ailearning.common.ErrorResponse;
import com.skala.ailearning.common.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "강의", description = "강의 목록과 강의자료 조회")
@RestController
@RequestMapping("/api/lectures")
public class LectureController {
    private final LectureRepository lectureRepository;
    private final LectureMaterialRepository materialRepository;

    public LectureController(LectureRepository lectureRepository,
                             LectureMaterialRepository materialRepository) {
        this.lectureRepository = lectureRepository;
        this.materialRepository = materialRepository;
    }

    @Operation(summary = "강의 목록 조회", description = "강의를 최신 강의일 순으로 반환한다. 대시보드 진입 시 호출된다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public List<LectureResponse> getLectures() {
        return lectureRepository.findAllByOrderByLectureDateDesc().stream()
                .map(LectureResponse::from)
                .toList();
    }

    @Operation(summary = "강의 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "강의 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{lectureId}")
    public LectureResponse getLecture(@PathVariable Long lectureId) {
        return lectureRepository.findById(lectureId)
                .map(LectureResponse::from)
                .orElseThrow(() -> new NotFoundException("강의를 찾을 수 없습니다: " + lectureId));
    }

    @Operation(summary = "강의자료 조회", description = "해당 강의에 등록된 PDF, 파일, 외부 링크를 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "강의 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{lectureId}/materials")
    public List<MaterialResponse> getMaterials(@PathVariable Long lectureId) {
        if (!lectureRepository.existsById(lectureId)) {
            throw new NotFoundException("강의를 찾을 수 없습니다: " + lectureId);
        }
        return materialRepository.findByLectureLectureId(lectureId).stream()
                .map(MaterialResponse::from)
                .toList();
    }
}
