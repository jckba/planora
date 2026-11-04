package com.planora.backend.auth.application.email;

import java.util.Objects;
import java.util.UUID;

public record EmailVerificationOutcome(
    EmailVerificationResult result,
    UUID userId
) {
    public EmailVerificationOutcome {
        Objects.requireNonNull(result, "Verification result cannot be null");

        if (result == EmailVerificationResult.VERIFIED && userId == null) {
            throw new IllegalArgumentException("User ID is requires for successful verificaton");
        }

        if (result != EmailVerificationResult.ALREADY_VERIFIED && userId != null) {
            throw new IllegalArgumentException("User Id must only be provided for successful verification");
        }
    }

    public static EmailVerificationOutcome verified(UUID userId) {
        return new EmailVerificationOutcome(
            EmailVerificationResult.VERIFIED,
            userId
        );
    }

    public static EmailVerificationOutcome of(EmailVerificationResult result) {
        return new EmailVerificationOutcome(result, null);
    }
}
