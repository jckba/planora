package com.planora.backend.auth.repository.jooq;

import com.planora.backend.auth.domain.EmailVerificationChallenge;
import com.planora.backend.auth.repository.EmailVerificationChallengeRepository;
import com.planora.persistence.jooq.tables.records.EmailVerificationChallengeRecord;
import jakarta.annotation.Nullable;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static com.planora.persistence.jooq.tables.EmailVerificationChallenge.EMAIL_VERIFICATION_CHALLENGE;

@Repository
public class JooqEmailVerificationChallengeRepository implements EmailVerificationChallengeRepository {

    private final DSLContext dsl;

    public JooqEmailVerificationChallengeRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public EmailVerificationChallenge save(EmailVerificationChallenge challenge) {
        dsl.insertInto(EMAIL_VERIFICATION_CHALLENGE)
            .set(EMAIL_VERIFICATION_CHALLENGE.ID, challenge.getId())
            .set(EMAIL_VERIFICATION_CHALLENGE.USER_ID, challenge.getUserId())
            .set(EMAIL_VERIFICATION_CHALLENGE.CODE_HASH, challenge.getCodeHash())
            .set(EMAIL_VERIFICATION_CHALLENGE.EXPIRES_AT,
                toOffsetDateTime(challenge.getExpiresAt()))
            .set(EMAIL_VERIFICATION_CHALLENGE.ATTEMPTS, challenge.getAttempts())
            .set(EMAIL_VERIFICATION_CHALLENGE.MAX_ATTEMPTS, challenge.getMaxAttempts())
            .set(EMAIL_VERIFICATION_CHALLENGE.CONSUMED_AT,
                toNullableOffsetDateTime(challenge.getConsumedAt()))
            .set(EMAIL_VERIFICATION_CHALLENGE.INVALIDATED_AT,
                toNullableOffsetDateTime(challenge.getInvalidatedAt()))
            .set(EMAIL_VERIFICATION_CHALLENGE.CREATED_AT,
                toOffsetDateTime(challenge.getCreatedAt()))
            .execute();
        return challenge;
    }

    @Override
    public Optional<EmailVerificationChallenge> findLatestByUserId(UUID userId) {
        return dsl.selectFrom(EMAIL_VERIFICATION_CHALLENGE)
            .where(EMAIL_VERIFICATION_CHALLENGE.USER_ID.eq(userId))
            .orderBy(EMAIL_VERIFICATION_CHALLENGE.CREATED_AT.desc())
            .limit(1)
            .fetchOptional()
            .map(this::mapToChallenge);
    }

    @Override
    public Optional<EmailVerificationChallenge> findLatestUnresolvedByUserIdForUpdate(UUID userId) {
        return dsl.selectFrom(EMAIL_VERIFICATION_CHALLENGE)
            .where(EMAIL_VERIFICATION_CHALLENGE.USER_ID.eq(userId))
            .and(EMAIL_VERIFICATION_CHALLENGE.CONSUMED_AT.isNull())
            .and(EMAIL_VERIFICATION_CHALLENGE.INVALIDATED_AT.isNull())
            .orderBy(EMAIL_VERIFICATION_CHALLENGE.CREATED_AT.desc())
            .limit(1)
            .forUpdate()
            .fetchOptional()
            .map(this::mapToChallenge);
    }

    @Override
    public void invalidateActiveByUserId(UUID userId, Instant invalidatedAt) {
        dsl.update(EMAIL_VERIFICATION_CHALLENGE)
            .set(EMAIL_VERIFICATION_CHALLENGE.INVALIDATED_AT, toOffsetDateTime(invalidatedAt))
            .where(EMAIL_VERIFICATION_CHALLENGE.USER_ID.eq(userId))
            .and(EMAIL_VERIFICATION_CHALLENGE.CONSUMED_AT.isNull())
            .and(EMAIL_VERIFICATION_CHALLENGE.INVALIDATED_AT.isNull())
            .execute();
    }

    @Override
    public EmailVerificationChallenge update(EmailVerificationChallenge challenge) {
        int updatedRows = dsl.update(EMAIL_VERIFICATION_CHALLENGE)
            .set(EMAIL_VERIFICATION_CHALLENGE.ATTEMPTS, challenge.getAttempts())
            .set(EMAIL_VERIFICATION_CHALLENGE.CONSUMED_AT, toNullableOffsetDateTime(challenge.getConsumedAt()))
            .set(EMAIL_VERIFICATION_CHALLENGE.INVALIDATED_AT, toNullableOffsetDateTime(challenge.getInvalidatedAt()))
            .where(EMAIL_VERIFICATION_CHALLENGE.ID.eq(challenge.getId()))
            .execute();

        if (updatedRows != 1) {
            throw new IllegalStateException("Expected to update one email verification challenge");
        }

        return challenge;
    }

    @Override
    public boolean existsCreatedSince(UUID userId, Instant since) {
        return dsl.fetchExists(
            dsl.selectOne()
                .from(EMAIL_VERIFICATION_CHALLENGE)
                .where(EMAIL_VERIFICATION_CHALLENGE.USER_ID.eq(userId))
                .and(EMAIL_VERIFICATION_CHALLENGE.CREATED_AT.ge(toOffsetDateTime(since)))
        );
    }

    private EmailVerificationChallenge mapToChallenge(EmailVerificationChallengeRecord record) {
        return EmailVerificationChallenge.restore(
            record.getId(),
            record.getUserId(),
            record.getCodeHash(),
            record.getExpiresAt().toInstant(),
            record.getAttempts(),
            record.getMaxAttempts(),
            toInstant(record.getConsumedAt()),
            toInstant(record.getInvalidatedAt()),
            record.getCreatedAt().toInstant()
        );
    }

    private static OffsetDateTime toOffsetDateTime(Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private static OffsetDateTime toNullableOffsetDateTime(
        @Nullable Instant instant) {

        return instant == null ? null : toOffsetDateTime(instant);
    }

    private static @Nullable Instant toInstant(
        @Nullable OffsetDateTime dateTime) {

        return dateTime == null ? null : dateTime.toInstant();
    }

}
