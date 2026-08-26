package com.planora.backend.purchase.domain;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Getter
public class PurchasePayment {
    private final UUID id;
    private final UUID accountId;
    private final BigDecimal amount;

    private PurchasePayment(
        UUID id,
        UUID accountId,
        BigDecimal amount
    ) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
    }

    public static PurchasePayment create(
        UUID accountId,
        BigDecimal amount
    ) {
        Objects.requireNonNull(
            accountId,
            "Account ID cannot be null"
        );

        Objects.requireNonNull(
            amount,
            "Amount cannot be null"
        );

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException(
                "Payment amount must be greater than zero"
            );
        }

        return new PurchasePayment(
            UUID.randomUUID(),
            accountId,
            amount
        );
    }

    public static PurchasePayment restore(
        UUID id,
        UUID accountId,
        BigDecimal amount
    ) {
        return new PurchasePayment(
            id,
            accountId,
            amount
        );
    }
}
