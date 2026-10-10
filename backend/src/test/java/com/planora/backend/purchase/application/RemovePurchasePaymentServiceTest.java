package com.planora.backend.purchase.application;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.purchase.application.payment.remove.RemovePurchasePaymentCommand;
import com.planora.backend.purchase.application.payment.remove.RemovePurchasePaymentService;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.domain.PurchasePayment;
import com.planora.backend.purchase.repository.PurchaseRepository;
import com.planora.backend.common.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemovePurchasePaymentServiceTest {
    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private RemovePurchasePaymentService removePurchasePaymentService;

    @Test
    void shouldRemovePaymentFromPurchase() {
        UUID userId = UUID.randomUUID();
        when(currentUser.userId()).thenReturn(userId);
        UUID purchaseId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        Purchase purchase =
            Purchase.create(
                userId,
                Instant.parse("2026-09-15T12:00:00Z"),
                "Purchase"
            );

        PurchasePayment payment =
            PurchasePayment.create(
                accountId,
                new BigDecimal("100.00")
            );

        purchase.addPayment(payment);

        RemovePurchasePaymentCommand command =
            new RemovePurchasePaymentCommand(
                purchaseId,
                payment.getId()
            );

        when(
            purchaseRepository.findByIdAndUserId(
                purchaseId,
                userId
            )
        ).thenReturn(Optional.of(purchase));

        when(
            purchaseRepository.update(purchase)
        ).thenReturn(purchase);

        Purchase result =
            removePurchasePaymentService.execute(command);

        assertSame(purchase, result);

        assertTrue(
            purchase.getPayments().isEmpty()
        );

        verify(
            purchaseRepository
        ).findByIdAndUserId(
            purchaseId,
            userId
        );

        verify(
            purchaseRepository
        ).update(purchase);
    }

    @Test
    void shouldThrowWhenPurchaseDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(currentUser.userId()).thenReturn(userId);
        UUID purchaseId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        RemovePurchasePaymentCommand command =
            new RemovePurchasePaymentCommand(
                purchaseId,
                paymentId
            );

        when(
            purchaseRepository.findByIdAndUserId(
                purchaseId,
                userId
            )
        ).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () ->
                removePurchasePaymentService.execute(command)
        );

        verify(
            purchaseRepository
        ).findByIdAndUserId(
            purchaseId,
            userId
        );

        verify(
            purchaseRepository,
            never()
        ).update(any(Purchase.class));
    }

    @Test
    void shouldRejectWhenPaymentDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(currentUser.userId()).thenReturn(userId);
        UUID purchaseId = UUID.randomUUID();

        Purchase purchase =
            Purchase.create(
                userId,
                Instant.parse("2026-09-15T12:00:00Z"),
                "Purchase"
            );

        RemovePurchasePaymentCommand command =
            new RemovePurchasePaymentCommand(
                purchaseId,
                UUID.randomUUID()
            );

        when(
            purchaseRepository.findByIdAndUserId(
                purchaseId,
                userId
            )
        ).thenReturn(Optional.of(purchase));

        assertThrows(
            IllegalArgumentException.class,
            () ->
                removePurchasePaymentService.execute(command)
        );

        verify(
            purchaseRepository
        ).findByIdAndUserId(
            purchaseId,
            userId
        );

        verify(
            purchaseRepository,
            never()
        ).update(any(Purchase.class));
    }

}
