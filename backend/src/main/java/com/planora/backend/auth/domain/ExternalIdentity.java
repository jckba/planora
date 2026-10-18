package com.planora.backend.auth.domain;

import java.time.Instant;
import java.util.UUID;

public record ExternalIdentity(
    UUID id,
    UUID userId,
    String provider,
    String providerSubject,
    Instant createdAt
) {
    public static ExternalIdentity google(
        UUID userId,
        String googleSubject
    ) {
        return new ExternalIdentity(
            UUID.randomUUID(),
            userId,
            "GOOGLE",
            googleSubject,
            Instant.now()
        );
    }
}
