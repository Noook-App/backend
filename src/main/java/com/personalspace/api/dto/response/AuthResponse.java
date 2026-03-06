package com.personalspace.api.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        long expiresAt,
        String name,
        String email
) {
    public AuthResponse(String accessToken, String refreshToken, long expiresIn, long expiresAt, String name, String email) {
        this(accessToken, refreshToken, "Bearer", expiresIn, expiresAt, name, email);
    }
}
