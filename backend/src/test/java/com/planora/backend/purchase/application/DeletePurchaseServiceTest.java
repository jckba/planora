package com.planora.backend.purchase.application;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.purchase.application.delete.DeletePurchaseService;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.repository.PurchaseRepository;
import com.planora.backend.common.security.CurrentUser;
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

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private DeletePurchaseService deletePurchaseService;

    @Test
    void shouldDeletePurchase() {
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

        deletePurchaseService.execute(purchaseId);

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
            () -> deletePurchaseService.execute(purchaseId)
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
