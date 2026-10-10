package com.planora.backend.purchase.application;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.purchase.application.item.remove.RemovePurchaseItemCommand;
import com.planora.backend.purchase.application.item.remove.RemovePurchaseItemService;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.domain.PurchaseItem;
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
class RemovePurchaseItemServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private RemovePurchaseItemService removePurchaseItemService;

    @Test
    void shouldRemoveItemFromPurchase() {
        UUID userId = UUID.randomUUID();
        when(currentUser.userId()).thenReturn(userId);
        UUID purchaseId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Purchase purchase =
            Purchase.create(
                userId,
                Instant.parse("2026-09-15T12:00:00Z"),
                "Purchase"
            );

        PurchaseItem item =
            PurchaseItem.create(
                categoryId,
                "Laptop",
                new BigDecimal("2"),
                new BigDecimal("1500.00")
            );

        purchase.addItem(item);

        RemovePurchaseItemCommand command =
            new RemovePurchaseItemCommand(
                purchaseId,
                item.getId()
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
            removePurchaseItemService.execute(command);

        assertSame(purchase, result);

        assertTrue(
            purchase.getItems().isEmpty()
        );

        assertEquals(
            0,
            BigDecimal.ZERO.compareTo(
                purchase.getTotal()
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
        UUID itemId = UUID.randomUUID();

        RemovePurchaseItemCommand command =
            new RemovePurchaseItemCommand(
                purchaseId,
                itemId
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
                removePurchaseItemService.execute(command)
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
    void shouldRejectWhenItemDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(currentUser.userId()).thenReturn(userId);
        UUID purchaseId = UUID.randomUUID();

        Purchase purchase =
            Purchase.create(
                userId,
                Instant.parse("2026-09-15T12:00:00Z"),
                "Purchase"
            );

        RemovePurchaseItemCommand command =
            new RemovePurchaseItemCommand(
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
                removePurchaseItemService.execute(command)
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
