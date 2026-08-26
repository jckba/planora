package com.planora.backend.purchase.application;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.purchase.application.get.GetPurchaseByIdService;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.repository.PurchaseRepository;
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

    @InjectMocks
    private GetPurchaseByIdService getPurchaseByIdService;

    @Test
    void shouldGetPurchaseById() {
        UUID userId = UUID.randomUUID();
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
            getPurchaseByIdService.execute(
                userId,
                purchaseId
            );

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
        UUID purchaseId = UUID.randomUUID();

        when(
            purchaseRepository.findByIdAndUserId(
                purchaseId,
                userId
            )
        ).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> getPurchaseByIdService.execute(
                userId,
                purchaseId
            )
        );

        verify(purchaseRepository)
            .findByIdAndUserId(
                purchaseId,
                userId
            );
    }
}
