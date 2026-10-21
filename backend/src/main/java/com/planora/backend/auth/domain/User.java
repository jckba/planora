package com.planora.backend.auth.domain;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Getter
public class User {

    private final UUID id;
    private @Nullable String username;
    private String email;
    private @Nullable String passwordHash;
    private String firstName;
    private String lastName;
    private @Nullable Short primaryCurrencyId;
    private @Nullable Instant emailVerifiedAt;
    private @Nullable Instant passwordUpdatedAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private int version;

    private User(UUID id, @Nullable String username, String email, @Nullable String passwordHash, String firstName, String lastName, @Nullable Short primaryCurrencyId, @Nullable Instant emailVerifiedAt, @Nullable Instant passwordUpdatedAt, Instant createdAt, Instant updatedAt, int version) {
        this.id = Objects.requireNonNull(id);
        this.username = username;
        this.email = Objects.requireNonNull(email);
        this.passwordHash = passwordHash;
        this.firstName = Objects.requireNonNull(firstName);
        this.lastName = Objects.requireNonNull(lastName);
        this.primaryCurrencyId = primaryCurrencyId;
        this.emailVerifiedAt = emailVerifiedAt;
        this.passwordUpdatedAt = passwordUpdatedAt;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.version = version;
    }

    public boolean isEmailVerified() {
        return emailVerifiedAt != null;
    }

    public boolean isProfileComplete() {
        return username != null && primaryCurrencyId != null;
    }

    public boolean hasLocalPassword() {
        return passwordHash != null;
    }

    public static User restore(
        UUID id,
        @Nullable String username,
        String email,
        @Nullable String passwordHash,
        String firstName,
        String lastName,
        @Nullable Short primaryCurrencyId,
        @Nullable Instant emailVerifiedAt,
        @Nullable Instant passwordUpdatedAt,
        Instant createdAt,
        Instant updatedAt,
        int version
    ) {
        return new User(
            id,
            username,
            email,
            passwordHash,
            firstName,
            lastName,
            primaryCurrencyId,
            emailVerifiedAt,
            passwordUpdatedAt,
            createdAt,
            updatedAt,
            version
        );
    }

    public static User createPendingRegistration(
        String email,
        String passwordHash,
        String firstName,
        String lastName
    ) {
        Instant now = Instant.now();

        return new User(
            UUID.randomUUID(),
            null,
            email,
            passwordHash,
            firstName,
            lastName,
            null,
            null,
            now,
            now,
            now,
            0
        );
    }

    public static User createExternal(
        String username,
        String email,
        String firstName,
        String lastName,
        Short primaryCurrencyId
    ) {
        Instant now = Instant.now();

        return new User(
            UUID.randomUUID(),
            username,
            email,
            null,
            firstName,
            lastName,
            primaryCurrencyId,
            null,
            null,
            now,
            now,
            0
        );
    }
}
