package com.planora.backend.purchase.application;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.purchase.application.payment.add.AddPurchasePaymentCommand;
import com.planora.backend.purchase.application.payment.add.AddPurchasePaymentService;
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
class AddPurchasePaymentServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private AddPurchasePaymentService addPurchasePaymentService;

    @Test
    void shouldAddPaymentToPurchase() {
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

        AddPurchasePaymentCommand command =
            new AddPurchasePaymentCommand(
                purchaseId,
                accountId,
                new BigDecimal("100.00")
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
            addPurchasePaymentService.execute(command);

        assertSame(purchase, result);

        assertEquals(
            1,
            purchase.getPayments().size()
        );

        assertEquals(
            accountId,
            purchase.getPayments()
                .getFirst()
                .getAccountId()
        );

        assertEquals(
            0,
            new BigDecimal("100.00")
                .compareTo(
                    purchase.getPayments()
                        .getFirst()
                        .getAmount()
                )
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

        AddPurchasePaymentCommand command =
            new AddPurchasePaymentCommand(
                purchaseId,
                UUID.randomUUID(),
                new BigDecimal("100.00")
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
                addPurchasePaymentService.execute(command)
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
    void shouldRejectDuplicateAccount() {
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

        purchase.addPayment(
            PurchasePayment.create(
                accountId,
                new BigDecimal("100.00")
            )
        );

        AddPurchasePaymentCommand command =
            new AddPurchasePaymentCommand(
                purchaseId,
                accountId,
                new BigDecimal("50.00")
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
                addPurchasePaymentService.execute(command)
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
