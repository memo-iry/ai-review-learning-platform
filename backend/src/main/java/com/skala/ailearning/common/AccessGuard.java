package com.skala.ailearning.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class AccessGuard {
    private final HttpServletRequest request;

    public AccessGuard(HttpServletRequest request) {
        this.request = request;
    }

    public SessionUser current() {
        HttpSession session = request.getSession(false);
        return session == null ? null : (SessionUser) session.getAttribute(SessionUser.KEY);
    }

    public SessionUser requireLogin() {
        SessionUser user = current();
        if (user == null) {
            throw new UnauthorizedException("로그인이 필요합니다");
        }
        return user;
    }

    public void requireSelfOrAdmin(Long targetUserId) {
        SessionUser user = requireLogin();
        if (!user.isAdmin() && !user.userId().equals(targetUserId)) {
            throw new ForbiddenException("다른 사용자의 데이터에는 접근할 수 없습니다");
        }
    }

    public void requireAdmin() {
        SessionUser user = requireLogin();
        if (!user.isAdmin()) {
            throw new ForbiddenException("운영자만 접근할 수 있습니다");
        }
    }
}
