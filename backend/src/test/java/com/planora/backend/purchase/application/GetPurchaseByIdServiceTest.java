package com.planora.backend.purchase.application;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.purchase.application.get.GetPurchaseByIdService;
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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPurchaseByIdServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private GetPurchaseByIdService getPurchaseByIdService;

    @Test
    void shouldGetPurchaseById() {
        UUID userId = UUID.randomUUID();
        when(currentUser.userId()).thenReturn(userId);
        UUID purchaseId = UUID.randomUUID();

        Purchase purchase = Purchase.create(
            userId,
            null,
            "Birthday purchase"
        );

        when(
            purchaseRepository.findByIdAndUserId(
                purchaseId,
                userId
            )
        ).thenReturn(Optional.of(purchase));

        Purchase result =
            getPurchaseByIdService.execute(purchaseId);

        assertSame(purchase, result);

        verify(purchaseRepository)
            .findByIdAndUserId(
                purchaseId,
                userId
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
            () -> getPurchaseByIdService.execute(purchaseId)
        );

        verify(purchaseRepository)
            .findByIdAndUserId(
                purchaseId,
                userId
            );
    }
}
