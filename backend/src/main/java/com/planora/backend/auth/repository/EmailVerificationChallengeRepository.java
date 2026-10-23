package com.planora.backend.auth.repository;

import com.planora.backend.auth.domain.EmailVerificationChallenge;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationChallengeRepository {

    EmailVerificationChallenge save(
        EmailVerificationChallenge challenge
    );

    Optional<EmailVerificationChallenge> findLatestByUserId(UUID userId);

    Optional<EmailVerificationChallenge> findLatestUnresolvedByUserIdForUpdate(UUID userId);

    void invalidateActiveByUserId(UUID userId, Instant invalidatedAt);

    EmailVerificationChallenge update(
        EmailVerificationChallenge challenge
    );

    boolean existsCreatedSince(UUID userId, Instant since);
}
