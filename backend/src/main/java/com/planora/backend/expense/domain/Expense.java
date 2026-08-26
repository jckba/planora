package com.planora.backend.expense.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class Expense {

    private UUID id;
    private UUID userId;
    private UUID accountId;
    private UUID categoryId;
    private String title;
    private String description;
    private BigDecimal amount;
    private Instant expenseDate;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private Integer version;

    private Expense(
        UUID id,
        UUID userId,
        UUID accountId,
        UUID categoryId,
        String title,
        String description,
        BigDecimal amount,
        Instant expenseDate,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        Integer version
    ) {
        this.id = id;
        this.userId = userId;
        this.accountId = accountId;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.version = version;
    }

    public static Expense create(
        UUID userId,
        UUID accountId,
        UUID categoryId,
        String title,
        String description,
        BigDecimal amount,
        Instant expenseDate
    ) {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(accountId, "Account ID cannot be null");
        Objects.requireNonNull(categoryId, "Category ID cannot be null");
        Objects.requireNonNull(title, "Title cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(expenseDate, "Expense date cannot be null");

        String normalizedTitle = normalizeRequiredText(title);
        String normalizedDescription = normalizeOptionalText(description);

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Instant now = Instant.now();

        return new Expense(
            UUID.randomUUID(),
            userId,
            accountId,
            categoryId,
            normalizedTitle,
            normalizedDescription,
            amount,
            expenseDate,
            now,
            now,
            null,
            0
        );
    }

    public static Expense restore(
        UUID id,
        UUID userId,
        UUID accountId,
        UUID categoryId,
        String title,
        String description,
        BigDecimal amount,
        Instant expenseDate,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        Integer version
    ) {
        return new Expense(
            id,
            userId,
            accountId,
            categoryId,
            title,
            description,
            amount,
            expenseDate,
            createdAt,
            updatedAt,
            deletedAt,
            version
        );
    }

    public void update(
        UUID accountId,
        UUID categoryId,
        String title,
        String description,
        BigDecimal amount,
        Instant expenseDate
    ) {
        Objects.requireNonNull(accountId, "Account ID cannot be null");
        Objects.requireNonNull(categoryId, "Category ID cannot be null");
        Objects.requireNonNull(title, "Title cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(expenseDate, "Expense date cannot be null");

        String normalizedTitle = normalizeRequiredText(title);

        String normalizedDescription = normalizeOptionalText(description);

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        this.setAccountId(accountId);
        this.setCategoryId(categoryId);
        this.setTitle(normalizedTitle);
        this.setDescription(normalizedDescription);
        this.setAmount(amount);
        this.setExpenseDate(expenseDate);

    }

    public void delete() {
        if (this.deletedAt != null) {
            throw new IllegalStateException("Expense is already deleted");
        }

        Instant now = Instant.now();

        this.deletedAt = now;
        this.updatedAt = now;
    }

    private static String normalizeRequiredText(String value) {
        String normalized = value.strip().replaceAll("\\s+", " ");

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Text cannot be blank");
        }

        return normalized;
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.strip().replaceAll("\\s+", " ");

        return normalized.isEmpty() ? null : normalized;
    }

}
