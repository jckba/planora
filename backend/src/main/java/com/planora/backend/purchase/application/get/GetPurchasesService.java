package com.planora.backend.purchase.application.get;


import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.common.security.CurrentUser;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetPurchasesService implements GetPurchasesUseCase {

    private final PurchaseRepository purchaseRepository;
    private final CurrentUser currentUser;

    public GetPurchasesService(PurchaseRepository purchaseRepository, CurrentUser currentUser) {
        this.purchaseRepository = purchaseRepository;
        this.currentUser = currentUser;
    }

    @Override
    public PageResult<Purchase> execute(PageRequest pageRequest) {
        UUID userId = currentUser.userId();
        return purchaseRepository.findByUserId(userId, pageRequest);
    }
}
