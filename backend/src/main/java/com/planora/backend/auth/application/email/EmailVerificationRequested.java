package com.planora.backend.auth.application.email;

import java.time.Instant;
import java.util.UUID;

public record EmailVerificationRequested(
    UUID userId,
    String email,
    String code,
    Instant expiresAt
) {
}
