package com.planora.backend.auth.security.oauth2;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PendingGoogleIdentityTest {

    @Test
    void shouldExpireAfterFiveMinutes() {

        Instant start =
            Instant.parse("2026-01-01T10:00:00Z");

        PendingGoogleIdentity pending =
            new PendingGoogleIdentity(
                "google-sub",
                "user@example.com",
                "User",
                "Test",
                start
            );

        Clock later = Clock.fixed(
            start.plusSeconds(301),
            ZoneOffset.UTC
        );

        assertTrue(pending.isExpired(later));
    }

    @Test
    void shouldBeValidBeforeExpiration() {

        Instant start =
            Instant.parse("2026-01-01T10:00:00Z");

        PendingGoogleIdentity pending =
            new PendingGoogleIdentity(
                "google-sub",
                "user@example.com",
                "User",
                "Test",
                start
            );

        Clock later = Clock.fixed(
            start.plusSeconds(60),
            ZoneOffset.UTC
        );

        assertFalse(pending.isExpired(later));
    }
}
