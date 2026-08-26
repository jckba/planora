package com.planora.backend.purchase.application;

import com.planora.backend.purchase.application.create.CreatePurchaseCommand;
import com.planora.backend.purchase.application.create.CreatePurchaseService;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.domain.PurchaseStatus;
import com.planora.backend.purchase.repository.PurchaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private CreatePurchaseService createPurchaseService;

    @Test
    void shouldCreatePurchase() {
        UUID userId = UUID.randomUUID();

        Instant expectedDate =
            Instant.parse(
                "2026-09-01T12:00:00Z"
            );

        CreatePurchaseCommand command =
            new CreatePurchaseCommand(
                expectedDate,
                "Birthday purchase"
            );

        Purchase savedPurchase =
            Purchase.create(
                userId,
                expectedDate,
                "Birthday purchase"
            );

        when(
            purchaseRepository.save(any(Purchase.class))
        ).thenReturn(savedPurchase);

        Purchase result =
            createPurchaseService.execute(
                userId,
                command
            );

        assertSame(
            savedPurchase,
            result
        );

        ArgumentCaptor<Purchase> captor =
            ArgumentCaptor.forClass(
                Purchase.class
            );

        verify(purchaseRepository)
            .save(captor.capture());

        Purchase createdPurchase =
            captor.getValue();

        assertEquals(
            userId,
            createdPurchase.getUserId()
        );

        assertEquals(
            PurchaseStatus.PENDING,
            createdPurchase.getStatus()
        );

        assertEquals(
            expectedDate,
            createdPurchase.getExpectedDate()
        );

        assertNull(
            createdPurchase.getPurchaseDate()
        );

        assertEquals(
            0,
            createdPurchase.getTotal()
                .compareTo(
                    java.math.BigDecimal.ZERO
                )
        );

        assertEquals(
            "Birthday purchase",
            createdPurchase.getNotes()
        );
    }

}
