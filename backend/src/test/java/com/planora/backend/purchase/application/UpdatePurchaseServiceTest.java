package com.planora.backend.purchase.application;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.purchase.application.update.UpdatePurchaseCommand;
import com.planora.backend.purchase.application.update.UpdatePurchaseService;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.repository.PurchaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UpdatePurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private UpdatePurchaseService updatePurchaseService;

    @Test
    void shouldUpdatePurchase() {
        UUID userId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();

        Purchase purchase =
            Purchase.create(
                userId,
                null,
                "Old notes"
            );

        Instant expectedDate =
            Instant.parse(
                "2026-09-15T12:00:00Z"
            );

        UpdatePurchaseCommand command =
            new UpdatePurchaseCommand(
                purchaseId,
                userId,
                expectedDate,
                "Updated notes"
            );

        when(
            purchaseRepository.findByIdAndUserId(
                purchaseId,
                userId
            )
        ).thenReturn(
            java.util.Optional.of(purchase)
        );

        when(
            purchaseRepository.update(purchase)
        ).thenReturn(purchase);

        Purchase result =
            updatePurchaseService.execute(command);

        assertSame(purchase, result);

        assertEquals(
            expectedDate,
            purchase.getExpectedDate()
        );

        assertEquals(
            "Updated notes",
            purchase.getNotes()
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
        UUID purchaseId = UUID.randomUUID();

        UpdatePurchaseCommand command =
            new UpdatePurchaseCommand(
                purchaseId,
                userId,
                null,
                "Updated notes"
            );

        when(
            purchaseRepository.findByIdAndUserId(
                purchaseId,
                userId
            )
        ).thenReturn(
            java.util.Optional.empty()
        );

        assertThrows(
            ResourceNotFoundException.class,
            () -> updatePurchaseService.execute(command)
        );

        verify(purchaseRepository)
            .findByIdAndUserId(
                purchaseId,
                userId
            );
    }
}
