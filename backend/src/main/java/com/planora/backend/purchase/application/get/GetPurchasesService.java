package com.planora.backend.purchase.application.get;


import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetPurchasesService implements GetPurchasesUseCase {

    private final PurchaseRepository purchaseRepository;

    public GetPurchasesService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @Override
    public PageResult<Purchase> execute(UUID userId, PageRequest pageRequest) {
        return purchaseRepository.findByUserId(userId, pageRequest);
    }
}
