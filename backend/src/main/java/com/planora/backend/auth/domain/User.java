package com.planora.backend.auth.domain;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Getter
public class User {

    private final UUID id;
    private String username;
    private String email;
    private @Nullable String passwordHash;
    private String firstName;
    private String lastName;
    private short primaryCurrencyId;
    private @Nullable Instant passwordUpdatedAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private int version;

    private User(UUID id, String username, String email, @Nullable String passwordHash, String firstName, String lastName, short primaryCurrencyId, @Nullable Instant passwordUpdatedAt, Instant createdAt, Instant updatedAt, int version) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.primaryCurrencyId = primaryCurrencyId;
        this.passwordUpdatedAt = passwordUpdatedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public boolean hasLocalPassword() {
        return passwordHash != null;
    }

    public static User create(String username, String email, String passwordHash, String firstName, String lastName, short primaryCurrencyId)
    {
        Instant now = Instant.now();
        return new User(
            UUID.randomUUID(),
            username,
            email,
            passwordHash,
            firstName,
            lastName,
            primaryCurrencyId,
            now,
            now,
            now,
            0
        );
    }

    public static User restore(
        UUID id,
        String username,
        String email,
        String passwordHash,
        String firstName,
        String lastName,
        short primaryCurrencyId,
        Instant passwordUpdatedAt,
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
            passwordUpdatedAt,
            createdAt,
            updatedAt,
            version
        );
    }

    public static User createExternal(
        String username,
        String email,
        String firstName,
        String lastName,
        short primaryCurrencyId
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
            now,
            now,
            0
        );
    }
}
