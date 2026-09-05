package com.skala.ailearning.mastery;

import com.skala.ailearning.common.AccessGuard;
import com.skala.ailearning.common.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "운영자", description = "교육생 전체 이해도 집계")
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    private final AccessGuard accessGuard;

    public AdminController(AdminService adminService, AccessGuard accessGuard) {
        this.adminService = adminService;
        this.accessGuard = accessGuard;
    }

    @Operation(
            summary = "교육생 이해도 집계",
            description = """
                    개념별 평균 이해도를 집계해 반환한다. 어느 주제에서 교육생들이
                    막히는지 확인하는 용도이며, 다음 수업 보완 지점을 찾는 데 쓴다.

                    운영자(ADMIN)만 접근할 수 있다. 교육생이 호출하면 403 이다.
                    개인 식별 정보는 반환하지 않고 집계값만 내려간다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "운영자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/overview")
    public AdminOverviewResponse overview() {
        accessGuard.requireAdmin();
        return adminService.overview();
    }
}
