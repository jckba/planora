package com.planora.backend.purchase.application.get;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetPurchaseByIdService implements GetPurchaseByIdUseCase {

    private final PurchaseRepository purchaseRepository;

    public GetPurchaseByIdService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @Override
    public Purchase execute(UUID userId, UUID purchaseId) {
        return purchaseRepository.findByIdAndUserId(purchaseId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
    }
}
