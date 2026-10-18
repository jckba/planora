package com.planora.backend.common.security.jwt;

public record IssuedAccessToken(
    String accessToken,
    String tokenType,
    long expiresIn
) {
}
