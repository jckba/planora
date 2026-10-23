package com.planora.backend.auth.domain;


import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class EmailVerificationChallenge {
    private final UUID id;
    private final UUID userId;
    private final String codeHash;
    private final Instant expiresAt;
    private int attempts;
    private final int maxAttempts;
    private @Nullable Instant consumedAt;
    private @Nullable Instant invalidatedAt;
    private final Instant createdAt;

    public EmailVerificationChallenge(UUID id, UUID userId, String codeHash, Instant expiresAt, int attempts, int maxAttempts, @Nullable Instant consumedAt, @Nullable Instant invalidatedAt, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.codeHash = Objects.requireNonNull(codeHash);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.attempts = attempts;
        this.maxAttempts = maxAttempts;
        this.consumedAt = consumedAt;
        this.invalidatedAt = invalidatedAt;
        this.createdAt = Objects.requireNonNull(createdAt);

        if (attempts < 0 || maxAttempts <= 0 || attempts > maxAttempts) {
            throw new IllegalArgumentException("Invalid verification attempts");
        }

        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("Expiration must follow creation");
        }

        if (consumedAt != null && invalidatedAt != null) {
            throw new IllegalArgumentException(
                "A challenge cannot be consumed and invalidated simultaneously"
            );
        }

    }

    public static EmailVerificationChallenge create(
        UUID userId,
        String codeHash,
        Instant now,
        Duration validity,
        int maxAttempts
    ) {
        if (validity.isZero() || validity.isNegative()) {
            throw new IllegalArgumentException("Validity must be positive");
        }
        return new EmailVerificationChallenge(
            UUID.randomUUID(),
            userId,
            codeHash,
            now.plus(validity),
            0,
            maxAttempts,
            null,
            null,
            now
        );
    }

    public static EmailVerificationChallenge restore(
        UUID id,
        UUID userId,
        String codeHash,
        Instant expiresAt,
        int attempts,
        int maxAttempts,
        @Nullable Instant consumedAt,
        @Nullable Instant invalidatedAt,
        Instant createdAt
    ) {
        return new EmailVerificationChallenge(
            id,
            userId,
            codeHash,
            expiresAt,
            attempts,
            maxAttempts,
            consumedAt,
            invalidatedAt,
            createdAt
        );
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public boolean isConsumed() {
        return consumedAt != null;
    }

    public boolean isInvalidated() {
        return invalidatedAt != null;
    }

    public boolean hasAttemptsRemaining() {
        return attempts < maxAttempts;
    }

    public boolean canBeVerifiedAt(Instant now) {
        return !isConsumed()
            && !isInvalidated()
            && !isExpired(now)
            && hasAttemptsRemaining();
    }

    public void invalidate(Instant now) {
        Objects.requireNonNull(now, "now cannot be null");

        if (isConsumed() || isInvalidated()) {
            throw new IllegalStateException(
                "Challenge is already in a terminal state"
            );
        }

        this.invalidatedAt = now;
    }

    public void registerFailedAttempt() {
        if (!hasAttemptsRemaining()) {
            throw new IllegalStateException("No attempts remaining");
        }

        attempts++;
    }

    public void consume(Instant now) {
        if (!canBeVerifiedAt(now)) {
            throw new IllegalArgumentException("Challenge can not be consumed");
        }

        consumedAt = now;
    }
}
