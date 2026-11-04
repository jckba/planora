package com.planora.backend.common.security.jwt.access;

public record IssuedAccessToken(
    String token,
    String tokenType,
    long expiresIn
) {
}
