package com.skala.ailearning.user;

public record LoginResponse(
        Long userId,
        String email,
        String name,
        String role
) {
    public static LoginResponse from(User user) {
        return new LoginResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name()
        );
    }
}
