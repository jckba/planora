package com.planora.backend.auth.security.oauth2;

import java.io.Serializable;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public record PendingGoogleIdentity(
    String subject,
    String email,
    String firstName,
    String lastName,
    Instant authenticatedAt
) implements Serializable {

    public static final String SESSION_KEY =
        "PLANORA_PENDING_GOOGLE_IDENTITY";

    public boolean isExpired(Clock clock) {
        Instant expiration = authenticatedAt.plus(Duration.ofMinutes(5));

        return !expiration.isAfter(clock.instant());
    }
}
