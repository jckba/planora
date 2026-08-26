package com.planora.backend.purchase.application;

import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.purchase.application.get.GetPurchasesService;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.repository.PurchaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPurchasesServiceTest {
    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private GetPurchasesService getPurchasesService;

    @Test
    void shouldGetPurchases() {
        UUID userId = UUID.randomUUID();

        PageRequest pageRequest =
            new PageRequest(0, 10);

        Purchase purchase =
            Purchase.create(
                userId,
                null,
                "Birthday purchase"
            );

        PageResult<Purchase> pageResult =
            new PageResult<>(
                List.of(purchase),
                0,
                10,
                1
            );

        when(
            purchaseRepository.findByUserId(
                userId,
                pageRequest
            )
        ).thenReturn(pageResult);

        PageResult<Purchase> result =
            getPurchasesService.execute(
                userId,
                pageRequest
            );

        assertSame(pageResult, result);

        verify(purchaseRepository)
            .findByUserId(
                userId,
                pageRequest
            );
    }
}
