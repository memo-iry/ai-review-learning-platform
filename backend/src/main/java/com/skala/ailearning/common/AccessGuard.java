package com.skala.ailearning.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AccessGuard {
    private final HttpServletRequest request;
    private final boolean enforce;

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
