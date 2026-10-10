package com.planora.backend.purchase.application;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.purchase.application.cancel.CancelPurchaseService;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.domain.PurchaseItem;
import com.planora.backend.purchase.domain.PurchasePayment;
import com.planora.backend.purchase.domain.PurchaseStatus;
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
class CancelPurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private CancelPurchaseService cancelPurchaseService;

    @Test
    void shouldCancelPurchase() {
        UUID userId = UUID.randomUUID();
        when(currentUser.userId()).thenReturn(userId);
        UUID purchaseId = UUID.randomUUID();

        Purchase purchase =
            Purchase.create(
                userId,
                null,
                null
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
            cancelPurchaseService.execute(purchaseId);

        assertSame(purchase, result);

        assertEquals(
            PurchaseStatus.CANCELLED,
            purchase.getStatus()
        );

        verify(purchaseRepository)
            .findByIdAndUserId(
                purchaseId,
                userId
            );

        verify(purchaseRepository)
            .update(purchase);
    }

    @Test
    void shouldThrowWhenPurchaseDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(currentUser.userId()).thenReturn(userId);
        UUID purchaseId = UUID.randomUUID();

        when(
            purchaseRepository.findByIdAndUserId(
                purchaseId,
                userId
            )
        ).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> cancelPurchaseService.execute(purchaseId)
        );

        verify(purchaseRepository)
            .findByIdAndUserId(
                purchaseId,
                userId
            );
        verify(
            purchaseRepository,
            never()
        ).update(any(Purchase.class));
    }

    @Test
    void shouldRejectCancellationOfCompletedPurchase() {
        UUID userId = UUID.randomUUID();
        when(currentUser.userId()).thenReturn(userId);
        UUID purchaseId = UUID.randomUUID();

        Purchase purchase =
            Purchase.create(
                userId,
                Instant.parse("2026-09-15T12:00:00Z"),
                "Purchase"
            );

        purchase.addItem(
            PurchaseItem.create(
                UUID.randomUUID(),
                "Laptop",
                BigDecimal.ONE,
                new BigDecimal("100.00")
            )
        );

        purchase.addPayment(
            PurchasePayment.create(
                UUID.randomUUID(),
                new BigDecimal("100.00")
            )
        );

        purchase.complete();

        when(
            purchaseRepository.findByIdAndUserId(
                purchaseId,
                userId
            )
        ).thenReturn(Optional.of(purchase));

        assertThrows(
            IllegalStateException.class,
            () ->
                cancelPurchaseService.execute(purchaseId)
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
