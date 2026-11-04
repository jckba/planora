package com.planora.backend.common.security.jwt.onboarding;

public record IssuedOnBoardingToken(
    String token,
    String tokenType,
    long expiresIn
) {
}
