package com.personalspace.api.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String name,
        String email
) {
    public AuthResponse(String accessToken, String refreshToken, long expiresIn, String name, String email) {
        this(accessToken, refreshToken, "Bearer", expiresIn, name, email);
    }
}
