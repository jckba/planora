package com.planora.backend.purchase.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseItemTest {

    @Test
    void shouldCreateValidPurchaseItem() {
        UUID categoryId = UUID.randomUUID();

        PurchaseItem item =
            PurchaseItem.create(
                categoryId,
                "Laptop",
                new BigDecimal("2"),
                new BigDecimal("1500.00")
            );

        assertNotNull(item.getId());
        assertEquals(
            categoryId,
            item.getCategoryId()
        );
        assertEquals(
            "Laptop",
            item.getName()
        );
        assertEquals(
            0,
            new BigDecimal("2")
                .compareTo(item.getQuantity())
        );
        assertEquals(
            0,
            new BigDecimal("1500.00")
                .compareTo(item.getUnitPrice())
        );
    }

    @Test
    void shouldCalculateSubtotal() {
        PurchaseItem item =
            PurchaseItem.create(
                UUID.randomUUID(),
                "Laptop",
                new BigDecimal("2"),
                new BigDecimal("1500.00")
            );

        assertEquals(
            0,
            new BigDecimal("3000.00")
                .compareTo(item.subtotal())
        );
    }

    @Test
    void shouldRejectZeroQuantity() {
        assertThrows(
            IllegalArgumentException.class,
            () -> PurchaseItem.create(
                UUID.randomUUID(),
                "Laptop",
                BigDecimal.ZERO,
                new BigDecimal("1500.00")
            )
        );
    }

    @Test
    void shouldRejectNegativeQuantity() {
        assertThrows(
            IllegalArgumentException.class,
            () -> PurchaseItem.create(
                UUID.randomUUID(),
                "Laptop",
                new BigDecimal("-1"),
                new BigDecimal("1500.00")
            )
        );
    }

    @Test
    void shouldRejectNegativeUnitPrice() {
        assertThrows(
            IllegalArgumentException.class,
            () -> PurchaseItem.create(
                UUID.randomUUID(),
                "Laptop",
                BigDecimal.ONE,
                new BigDecimal("-1.00")
            )
        );
    }

    @Test
    void shouldNormalizeNameWhitespace() {
        PurchaseItem item =
            PurchaseItem.create(
                UUID.randomUUID(),
                "  Gaming   Laptop  ",
                BigDecimal.ONE,
                new BigDecimal("1500.00")
            );

        assertEquals(
            "Gaming Laptop",
            item.getName()
        );
    }


}
