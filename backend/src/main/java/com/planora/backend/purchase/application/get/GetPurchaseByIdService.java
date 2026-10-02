package com.planora.backend.purchase.application.get;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.common.security.CurrentUser;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetPurchaseByIdService implements GetPurchaseByIdUseCase {

    private final PurchaseRepository purchaseRepository;
    private final CurrentUser currentUser;

    public GetPurchaseByIdService(PurchaseRepository purchaseRepository, CurrentUser currentUser) {
        this.purchaseRepository = purchaseRepository;
        this.currentUser = currentUser;
    }

    @Override
    public Purchase execute(UUID purchaseId) {
        UUID userId = currentUser.userId();
        return purchaseRepository.findByIdAndUserId(purchaseId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
    }
}
