package com.planora.backend.purchase.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PurchaseTest {
    @Test
    void shouldCreatePendingPurchase() {
        UUID userId = UUID.randomUUID();
        Instant expectedDate =
            Instant.parse("2026-09-01T12:00:00Z");

        Purchase purchase =
            Purchase.create(
                userId,
                expectedDate,
                "Birthday purchase"
            );

        assertNotNull(purchase.getId());
        assertEquals(userId, purchase.getUserId());
        assertEquals(
            PurchaseStatus.PENDING,
            purchase.getStatus()
        );
        assertEquals(
            expectedDate,
            purchase.getExpectedDate()
        );
        assertNull(purchase.getPurchaseDate());
        assertEquals(
            0,
            BigDecimal.ZERO.compareTo(
                purchase.getTotal()
            )
        );
        assertEquals(
            "Birthday purchase",
            purchase.getNotes()
        );
        assertTrue(purchase.getItems().isEmpty());
        assertTrue(purchase.getPayments().isEmpty());
        assertEquals(0, purchase.getVersion());
    }

    @Test
    void shouldRejectNullUserId() {
        assertThrows(
            NullPointerException.class,
            () -> Purchase.create(
                null,
                null,
                null
            )
        );
    }

    @Test
    void shouldAddItemAndRecalculateTotal() {
        Purchase purchase =
            Purchase.create(
                UUID.randomUUID(),
                null,
                null
            );

        PurchaseItem item =
            PurchaseItem.create(
                UUID.randomUUID(),
                "Laptop",
                new BigDecimal("2"),
                new BigDecimal("1500.00")
            );

        purchase.addItem(item);

        assertEquals(1, purchase.getItems().size());

        assertEquals(
            0,
            new BigDecimal("3000.00")
                .compareTo(purchase.getTotal())
        );
    }

    @Test
    void shouldCalculateTotalFromAllItems() {
        Purchase purchase =
            Purchase.create(
                UUID.randomUUID(),
                null,
                null
            );

        purchase.addItem(
            PurchaseItem.create(
                UUID.randomUUID(),
                "Laptop",
                BigDecimal.ONE,
                new BigDecimal("1500.00")
            )
        );

        purchase.addItem(
            PurchaseItem.create(
                UUID.randomUUID(),
                "Mouse",
                BigDecimal.ONE,
                new BigDecimal("50.00")
            )
        );

        assertEquals(
            0,
            new BigDecimal("1550.00")
                .compareTo(purchase.getTotal())
        );
    }

    @Test
    void shouldNotAddItemToCompletedPurchase() {
        Purchase purchase =
            createCompletablePurchase();

        purchase.complete();

        assertThrows(
            IllegalStateException.class,
            () -> purchase.addItem(
                PurchaseItem.create(
                    UUID.randomUUID(),
                    "Mouse",
                    BigDecimal.ONE,
                    new BigDecimal("50.00")
                )
            )
        );
    }

    private Purchase createCompletablePurchase() {
        Purchase purchase =
            Purchase.create(
                UUID.randomUUID(),
                null,
                null
            );

        PurchaseItem item =
            PurchaseItem.create(
                UUID.randomUUID(),
                "Laptop",
                BigDecimal.ONE,
                new BigDecimal("1500.00")
            );

        purchase.addItem(item);

        purchase.addPayment(
            PurchasePayment.create(
                UUID.randomUUID(),
                new BigDecimal("1500.00")
            )
        );

        return purchase;
    }

    @Test
    void shouldCompletePurchase() {
        Purchase purchase =
            createCompletablePurchase();

        purchase.complete();

        assertEquals(
            PurchaseStatus.COMPLETED,
            purchase.getStatus()
        );

        assertNotNull(
            purchase.getPurchaseDate()
        );
    }

    @Test
    void shouldNotCompletePurchaseWithoutItems() {
        Purchase purchase =
            Purchase.create(
                UUID.randomUUID(),
                null,
                null
            );

        assertThrows(
            IllegalStateException.class,
            purchase::complete
        );
    }

    @Test
    void shouldNotCompleteWhenPaymentsDoNotMatchTotal() {
        Purchase purchase =
            Purchase.create(
                UUID.randomUUID(),
                null,
                null
            );

        purchase.addItem(
            PurchaseItem.create(
                UUID.randomUUID(),
                "Laptop",
                BigDecimal.ONE,
                new BigDecimal("1500.00")
            )
        );

        purchase.addPayment(
            PurchasePayment.create(
                UUID.randomUUID(),
                new BigDecimal("1000.00")
            )
        );

        assertThrows(
            IllegalStateException.class,
            purchase::complete
        );
    }

    @Test
    void shouldCancelPendingPurchase() {
        Purchase purchase =
            Purchase.create(
                UUID.randomUUID(),
                null,
                null
            );

        purchase.cancel();

        assertEquals(
            PurchaseStatus.CANCELLED,
            purchase.getStatus()
        );
    }

    @Test
    void shouldNotCancelCompletedPurchase() {
        Purchase purchase =
            createCompletablePurchase();

        purchase.complete();

        assertThrows(
            IllegalStateException.class,
            purchase::cancel
        );
    }

    @Test
    void shouldNotAllowSameAccountForMultiplePayments() {
        UUID accountId = UUID.randomUUID();

        Purchase purchase =
            Purchase.create(
                UUID.randomUUID(),
                null,
                null
            );

        purchase.addPayment(
            PurchasePayment.create(
                accountId,
                new BigDecimal("50.00")
            )
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> purchase.addPayment(
                PurchasePayment.create(
                    accountId,
                    new BigDecimal("30.00")
                )
            )
        );
    }

    @Test
    void shouldRemoveItemAndRecalculateTotal() {
        Purchase purchase =
            Purchase.create(
                UUID.randomUUID(),
                null,
                null
            );

        PurchaseItem laptop =
            PurchaseItem.create(
                UUID.randomUUID(),
                "Laptop",
                BigDecimal.ONE,
                new BigDecimal("1500.00")
            );

        PurchaseItem mouse =
            PurchaseItem.create(
                UUID.randomUUID(),
                "Mouse",
                BigDecimal.ONE,
                new BigDecimal("50.00")
            );

        purchase.addItem(laptop);
        purchase.addItem(mouse);

        purchase.removeItem(laptop.getId());

        assertEquals(1, purchase.getItems().size());

        assertEquals(
            0,
            new BigDecimal("50.00")
                .compareTo(purchase.getTotal())
        );
    }

    @Test
    void shouldRejectRemovingUnknownItem() {
        Purchase purchase =
            Purchase.create(
                UUID.randomUUID(),
                null,
                null
            );

        assertThrows(
            IllegalArgumentException.class,
            () -> purchase.removeItem(
                UUID.randomUUID()
            )
        );
    }

    @Test
    void shouldRestorePurchase() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Instant createdAt =
            Instant.parse("2026-08-01T10:00:00Z");

        Instant updatedAt =
            Instant.parse("2026-08-02T10:00:00Z");

        PurchaseItem item =
            PurchaseItem.create(
                UUID.randomUUID(),
                "Laptop",
                BigDecimal.ONE,
                new BigDecimal("1500.00")
            );

        PurchasePayment payment =
            PurchasePayment.create(
                UUID.randomUUID(),
                new BigDecimal("1500.00")
            );

        Purchase purchase =
            Purchase.restore(
                id,
                userId,
                PurchaseStatus.COMPLETED,
                null,
                Instant.parse(
                    "2026-08-02T09:00:00Z"
                ),
                new BigDecimal("1500.00"),
                "Restored purchase",
                createdAt,
                updatedAt,
                4,
                List.of(item),
                List.of(payment)
            );

        assertEquals(id, purchase.getId());
        assertEquals(userId, purchase.getUserId());
        assertEquals(
            PurchaseStatus.COMPLETED,
            purchase.getStatus()
        );
        assertEquals(
            0,
            new BigDecimal("1500.00")
                .compareTo(purchase.getTotal())
        );
        assertEquals(
            createdAt,
            purchase.getCreatedAt()
        );
        assertEquals(
            updatedAt,
            purchase.getUpdatedAt()
        );
        assertEquals(4, purchase.getVersion());

        assertEquals(
            1,
            purchase.getItems().size()
        );

        assertEquals(
            1,
            purchase.getPayments().size()
        );
    }


}
