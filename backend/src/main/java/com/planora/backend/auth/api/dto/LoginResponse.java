package com.planora.backend.auth.api.dto;

public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn
) {
}
