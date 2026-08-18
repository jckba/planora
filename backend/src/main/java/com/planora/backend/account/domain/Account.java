package com.planora.backend.account.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class Account {
    private UUID id;
    private UUID userId;
    private Short accountTypeId;
    private Short currencyId;
    private String name;
    private BigDecimal balance;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer version;

    private Account(UUID id, UUID userId, Short accountTypeId, Short currencyId, String name, BigDecimal balance, Instant createdAt, Instant updatedAt, Integer version) {
        this.id = id;
        this.userId = userId;
        this.accountTypeId = accountTypeId;
        this.currencyId = currencyId;
        this.name = name;
        this.balance = balance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public static Account create(UUID userId, Short accountTypeId, Short currencyId, String name) {

        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(accountTypeId, "Account type ID cannot be null");
        Objects.requireNonNull(currencyId, "Currency ID cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");

        String normalizedName = name
            .trim()
            .replaceAll("\\s+", " ");

        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }

        Instant now = Instant.now();
        return new Account(
            UUID.randomUUID(),
            userId,
            accountTypeId,
            currencyId,
            normalizedName,
            BigDecimal.ZERO,
            now,
            now,
            0);
    }

    public void update(
        Short accountTypeId,
        Short currencyId,
        String name)
    {
        this.accountTypeId = accountTypeId;
        this.currencyId = currencyId;
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public static Account restore(UUID id, UUID userId, Short accountTypeId, Short currencyId, String name, BigDecimal balance, Instant createdAt, Instant updatedAt, Integer version) {
        return new Account(id, userId, accountTypeId, currencyId, name, balance, createdAt, updatedAt, version);
    }


}

