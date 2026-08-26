package com.planora.backend.purchase.application;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.purchase.application.delete.DeletePurchaseService;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.repository.PurchaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeletePurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private DeletePurchaseService deletePurchaseService;

    @Test
    void shouldDeletePurchase() {
        UUID userId = UUID.randomUUID();
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

        deletePurchaseService.execute(
            userId,
            purchaseId
        );

        verify(purchaseRepository)
            .findByIdAndUserId(
                purchaseId,
                userId
            );

        verify(purchaseRepository)
            .delete(purchase);

        verifyNoMoreInteractions(
            purchaseRepository
        );
    }

    @Test
    void shouldThrowWhenPurchaseDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();

        when(
            purchaseRepository.findByIdAndUserId(
                purchaseId,
                userId
            )
        ).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> deletePurchaseService.execute(
                userId,
                purchaseId
            )
        );

        verify(purchaseRepository)
            .findByIdAndUserId(
                purchaseId,
                userId
            );

        verify(
            purchaseRepository,
            never()
        ).delete(any(Purchase.class));
    }
}
