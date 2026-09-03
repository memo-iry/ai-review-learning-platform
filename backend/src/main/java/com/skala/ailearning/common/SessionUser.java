package com.skala.ailearning.common;

import java.io.Serializable;

public record SessionUser(Long userId, String email, String name, String role) implements Serializable {

    public static final String KEY = "LOGIN_USER";

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
