package com.planora.backend.purchase.application;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.purchase.application.item.add.AddPurchaseItemCommand;
import com.planora.backend.purchase.application.item.add.AddPurchaseItemService;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.repository.PurchaseRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddPurchaseItemServiceTest {
    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private AddPurchaseItemService addPurchaseItemService;

    @Test
    void shouldAddItemToPurchase() {
        UUID userId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Purchase purchase =
            Purchase.create(
                userId,
                Instant.parse("2026-09-15T12:00:00Z"),
                "Purchase"
            );

        AddPurchaseItemCommand command =
            new AddPurchaseItemCommand(
                purchaseId,
                userId,
                categoryId,
                "Laptop",
                new BigDecimal("2"),
                new BigDecimal("1500.00")
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
            addPurchaseItemService.execute(command);

        assertSame(purchase, result);

        assertEquals(
            1,
            purchase.getItems().size()
        );

        assertEquals(
            categoryId,
            purchase.getItems().getFirst().getCategoryId()
        );

        assertEquals(
            "Laptop",
            purchase.getItems().getFirst().getName()
        );

        assertEquals(
            0,
            new BigDecimal("2")
                .compareTo(
                    purchase.getItems().getFirst().getQuantity()
                )
        );

        assertEquals(
            0,
            new BigDecimal("1500.00")
                .compareTo(
                    purchase.getItems().getFirst().getUnitPrice()
                )
        );

        assertEquals(
            0,
            new BigDecimal("3000.00")
                .compareTo(
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
        UUID purchaseId = UUID.randomUUID();

        AddPurchaseItemCommand command =
            new AddPurchaseItemCommand(
                purchaseId,
                userId,
                UUID.randomUUID(),
                "Laptop",
                new BigDecimal("1"),
                new BigDecimal("1500.00")
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
                addPurchaseItemService.execute(command)
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
