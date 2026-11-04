package com.planora.backend.auth.api.dto.register;

public record EmailVerificationConfirmationResponse(
    String message,
    String token,
    String tokenType,
    long expiresIn
) {
}
