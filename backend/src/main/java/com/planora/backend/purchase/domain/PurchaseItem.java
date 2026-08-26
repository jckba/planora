package com.planora.backend.purchase.domain;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Getter
public class PurchaseItem {
    private final UUID id;
    private final UUID categoryId;
    private final String name;
    private final BigDecimal quantity;
    private final BigDecimal unitPrice;

    public PurchaseItem(UUID id, UUID categoryId, String name, BigDecimal quantity, BigDecimal unitPrice) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public static PurchaseItem create(UUID categoryId, String name, BigDecimal quantity, BigDecimal unitPrice) {
        Objects.requireNonNull(categoryId, "Category ID cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        Objects.requireNonNull(unitPrice, "Unit price cannot be null");

        String normalizedName = normalizeRequiredText(name);

        if (quantity.signum() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }

        return new PurchaseItem(UUID.randomUUID(), categoryId, normalizedName, quantity, unitPrice);
    }

    public static PurchaseItem restore(
        UUID id,
        UUID categoryId,
        String name,
        BigDecimal quantity,
        BigDecimal unitPrice
    ) {
        return new PurchaseItem(
            id,
            categoryId,
            name,
            quantity,
            unitPrice
        );
    }

    public BigDecimal subtotal() {
        return quantity.multiply(unitPrice);
    }

    private static String normalizeRequiredText (String value) {
        String normalized = value.strip().replaceAll("\\s+", " ");

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Text cannot be blank");
        }

        return normalized;
    }
}
