package com.planora.backend.purchase.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PurchasePaymentTest {
    @Test
    void shouldCreateValidPayment() {
        UUID accountId = UUID.randomUUID();

        PurchasePayment payment =
            PurchasePayment.create(
                accountId,
                new BigDecimal("50.00")
            );

        assertNotNull(payment.getId());
        assertEquals(
            accountId,
            payment.getAccountId()
        );
        assertEquals(
            0,
            new BigDecimal("50.00")
                .compareTo(payment.getAmount())
        );
    }

    @Test
    void shouldRejectZeroAmount() {
        assertThrows(
            IllegalArgumentException.class,
            () -> PurchasePayment.create(
                UUID.randomUUID(),
                BigDecimal.ZERO
            )
        );
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThrows(
            IllegalArgumentException.class,
            () -> PurchasePayment.create(
                UUID.randomUUID(),
                new BigDecimal("-10.00")
            )
        );
    }

    @Test
    void shouldRejectNullAccount() {
        assertThrows(
            NullPointerException.class,
            () -> PurchasePayment.create(
                null,
                new BigDecimal("50.00")
            )
        );
    }

    @Test
    void shouldRejectNullAmount() {
        assertThrows(
            NullPointerException.class,
            () -> PurchasePayment.create(
                UUID.randomUUID(),
                null
            )
        );
    }

}
