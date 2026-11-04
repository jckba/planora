package com.planora.backend.auth.application.email;

public enum EmailVerificationResult {
    VERIFIED,
    INVALID_OR_EXPIRED,
    TOO_MANY_ATTEMPTS,
    ALREADY_VERIFIED
}
