package com.skala.ailearning.user;

import com.skala.ailearning.common.AccessGuard;
import com.skala.ailearning.common.ErrorResponse;
import com.skala.ailearning.common.SessionUser;
import com.skala.ailearning.common.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "로그인 · 세션 확인 · 로그아웃")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AccessGuard accessGuard;

    public AuthController(AuthService authService, AccessGuard accessGuard) {
        this.authService = authService;
        this.accessGuard = accessGuard;
    }

    @Operation(
            summary = "로그인",
            description = """
                    이메일과 비밀번호를 확인하고 세션을 만든다.
                    비밀번호는 BCrypt 해시로 저장되어 있으며 평문을 되돌려주지 않는다.

                    이후 요청은 세션 쿠키로 사용자를 식별한다. userId 를 파라미터로 받지 않는 이유는,
                    받는 순간 그것은 인증이 아니라 요청값이 되어 위조할 수 있기 때문이다.

                    브라우저에서는 fetch 에 credentials: 'include' 가 필요하다.

                    데모 계정
                      learner@skala.com / demo   교육생
                      admin@skala.com   / demo   운영자
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request,
                               HttpServletRequest servletRequest) {
        LoginResponse response = authService.login(request);

        HttpSession session = servletRequest.getSession(true);
        session.setAttribute(SessionUser.KEY, new SessionUser(
                response.userId(), response.email(), response.name(), response.role()));

        return response;
    }

    @Operation(
            summary = "현재 로그인 사용자",
            description = """
                    세션에 담긴 사용자를 반환한다. 새로고침 후 로그인 상태를 복원할 때 쓴다.
                    클라이언트가 사용자 정보를 따로 보관하지 않아도 된다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 상태"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me")
    public SessionUser me() {
        SessionUser user = accessGuard.current();
        if (user == null) {
            throw new UnauthorizedException("로그인이 필요합니다");
        }
        return user;
    }

    @Operation(summary = "로그아웃", description = "세션을 파기한다.")
    @ApiResponse(responseCode = "204", description = "로그아웃 완료")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest servletRequest) {
        HttpSession session = servletRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
