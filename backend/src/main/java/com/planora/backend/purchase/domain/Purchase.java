package com.planora.backend.purchase.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class Purchase {
    private UUID id;
    private UUID userId;
    private PurchaseStatus status;
    private Instant expectedDate;
    private Instant purchaseDate;
    private BigDecimal total;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer version;

    private final List<PurchaseItem> items;
    private final List<PurchasePayment> payments;

    private Purchase(
        UUID id,
        UUID userId,
        PurchaseStatus status,
        Instant expectedDate,
        Instant purchaseDate,
        BigDecimal total,
        String notes,
        Instant createdAt,
        Instant updatedAt,
        Integer version,
        List<PurchaseItem> items,
        List<PurchasePayment> payments
    ) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.expectedDate = expectedDate;
        this.purchaseDate = purchaseDate;
        this.total = total;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
        this.items = new ArrayList<>(items);
        this.payments = new ArrayList<>(payments);
    }

    public static Purchase create(
        UUID userId,
        Instant expectedDate,
        String notes
    ) {
        Objects.requireNonNull(
            userId,
            "User ID cannot be null"
        );

        Instant now = Instant.now();

        return new Purchase(
            UUID.randomUUID(),
            userId,
            PurchaseStatus.PENDING,
            expectedDate,
            null,
            BigDecimal.ZERO,
            normalizeOptionalText(notes),
            now,
            now,
            0,
            List.of(),
            List.of()
        );
    }

    public static Purchase restore(
        UUID id,
        UUID userId,
        PurchaseStatus status,
        Instant expectedDate,
        Instant purchaseDate,
        BigDecimal total,
        String notes,
        Instant createdAt,
        Instant updatedAt,
        Integer version,
        List<PurchaseItem> items,
        List<PurchasePayment> payments
    ) {
        Objects.requireNonNull(
            id,
            "Purchase ID cannot be null"
        );

        Objects.requireNonNull(
            userId,
            "User ID cannot be null"
        );

        Objects.requireNonNull(
            status,
            "Purchase status cannot be null"
        );

        Objects.requireNonNull(
            total,
            "Purchase total cannot be null"
        );

        Objects.requireNonNull(
            createdAt,
            "Created at cannot be null"
        );

        Objects.requireNonNull(
            updatedAt,
            "Updated at cannot be null"
        );

        Objects.requireNonNull(
            version,
            "Version cannot be null"
        );

        Objects.requireNonNull(
            items,
            "Items cannot be null"
        );

        Objects.requireNonNull(
            payments,
            "Payments cannot be null"
        );

        return new Purchase(
            id,
            userId,
            status,
            expectedDate,
            purchaseDate,
            total,
            normalizeOptionalText(notes),
            createdAt,
            updatedAt,
            version,
            items,
            payments
        );
    }

    public void addItem(PurchaseItem item) {
        Objects.requireNonNull(
            item,
            "Purchase item cannot be null"
        );

        ensurePending();

        items.add(item);

        recalculateTotal();
        touch();
    }

    public void removeItem(UUID itemId) {
        Objects.requireNonNull(
            itemId,
            "Item ID cannot be null"
        );

        ensurePending();

        boolean removed = items.removeIf(
            item -> item.getId().equals(itemId)
        );

        if (!removed) {
            throw new IllegalArgumentException(
                "Purchase item not found"
            );
        }

        recalculateTotal();
        touch();
    }

    public void updateDetails(
        Instant expectedDate,
        String notes
    ) {
        ensurePending();

        this.expectedDate = expectedDate;
        this.notes = normalizeOptionalText(notes);

        touch();
    }


    public void addPayment(PurchasePayment payment) {
        Objects.requireNonNull(
            payment,
            "Purchase payment cannot be null"
        );

        ensurePending();

        boolean accountAlreadyUsed =
            payments.stream()
                .anyMatch(
                    existing ->
                        existing.getAccountId()
                            .equals(payment.getAccountId())
                );

        if (accountAlreadyUsed) {
            throw new IllegalArgumentException(
                "Account already used for this purchase"
            );
        }

        payments.add(payment);
        touch();
    }

    public void removePayment(UUID paymentId) {
        Objects.requireNonNull(
            paymentId,
            "Payment ID cannot be null"
        );

        ensurePending();

        boolean removed = payments.removeIf(
            payment -> payment.getId().equals(paymentId)
        );

        if (!removed) {
            throw new IllegalArgumentException(
                "Purchase payment not found"
            );
        }

        touch();
    }

    public void complete() {
        if (status != PurchaseStatus.PENDING) {
            throw new IllegalStateException(
                "Only pending purchases can be completed"
            );
        }

        if (items.isEmpty()) {
            throw new IllegalStateException(
                "Purchase must contain at least one item"
            );
        }

        if (total.signum() <= 0) {
            throw new IllegalStateException(
                "Purchase total must be greater than zero"
            );
        }

        BigDecimal paidAmount =
            payments.stream()
                .map(PurchasePayment::getAmount)
                .reduce(
                    BigDecimal.ZERO,
                    BigDecimal::add
                );

        if (paidAmount.compareTo(total) != 0) {
            throw new IllegalStateException(
                "Purchase payments must equal purchase total"
            );
        }

        status = PurchaseStatus.COMPLETED;
        purchaseDate = Instant.now();

        touch();
    }

    public void cancel() {
        if (status != PurchaseStatus.PENDING) {
            throw new IllegalStateException(
                "Only pending purchases can be cancelled"
            );
        }

        status = PurchaseStatus.CANCELLED;

        touch();
    }

    private void recalculateTotal() {
        total = items.stream()
            .map(PurchaseItem::subtotal)
            .reduce(
                BigDecimal.ZERO,
                BigDecimal::add
            );
    }

    private void ensurePending() {
        if (status != PurchaseStatus.PENDING) {
            throw new IllegalStateException(
                "Purchase can only be modified while pending"
            );
        }
    }

    private void touch() {
        updatedAt = Instant.now();
    }

    private static String normalizeOptionalText(
        String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
            value.strip().replaceAll("\\s+", " ");

        return normalized.isEmpty()
            ? null
            : normalized;
    }

    public List<PurchaseItem> getItems() {
        return List.copyOf(items);
    }

    public List<PurchasePayment> getPayments() {
        return List.copyOf(payments);
    }

    public void incrementVersion() {
        version++;
    }
}
