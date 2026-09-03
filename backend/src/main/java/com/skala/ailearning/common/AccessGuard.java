package com.skala.ailearning.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AccessGuard {

    private final HttpServletRequest request;
    private final boolean enforce;

    /**
     * app.security.enforce 는 기본값을 두지 않는다.
     * 보안 스위치가 설정 누락으로 조용히 켜지거나 꺼지면 안 된다.
     * 값이 없으면 기동 단계에서 실패한다.
     */
    public AccessGuard(HttpServletRequest request,
                       @Value("${app.security.enforce}") boolean enforce) {
        this.request = request;
        this.enforce = enforce;
    }

    public SessionUser current() {
        HttpSession session = request.getSession(false);
        return session == null ? null : (SessionUser) session.getAttribute(SessionUser.KEY);
    }

    public SessionUser requireLogin() {
        SessionUser user = current();
        if (user == null) {
            if (!enforce) {
                return null;
            }
            throw new UnauthorizedException("로그인이 필요합니다");
        }
        return user;
    }

    public void requireSelfOrAdmin(Long targetUserId) {
        SessionUser user = requireLogin();
        if (user == null) {
            return;
        }
        if (!user.isAdmin() && !user.userId().equals(targetUserId)) {
            throw new ForbiddenException("다른 사용자의 데이터에는 접근할 수 없습니다");
        }
    }

    /**
     * 운영자 전용 경로는 enforce 스위치와 무관하게 항상 막는다.
     * 스위치는 프론트에 로그인이 붙기 전까지의 임시 조치이고,
     * 프론트는 운영자 API 를 호출하지 않으므로 완화할 이유가 없다.
     */
    public void requireAdmin() {
        SessionUser user = current();
        if (user == null) {
            throw new UnauthorizedException("로그인이 필요합니다");
        }
        if (!user.isAdmin()) {
            throw new ForbiddenException("운영자만 접근할 수 있습니다");
        }
    }

    public boolean isEnforced() {
        return enforce;
    }
}
