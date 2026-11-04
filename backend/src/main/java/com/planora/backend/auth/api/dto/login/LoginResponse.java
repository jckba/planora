package com.planora.backend.auth.api.dto.login;

public record LoginResponse(
    String token,
    String tokenType,
    long expiresIn
) {
}
