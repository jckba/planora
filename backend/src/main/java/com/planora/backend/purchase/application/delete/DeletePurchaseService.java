package com.planora.backend.purchase.application.delete;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeletePurchaseService implements DeletePurchaseUseCase {

    private final PurchaseRepository purchaseRepository;

    public DeletePurchaseService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @Override
    public void execute(UUID userId, UUID purchaseId) {
        Purchase purchase = purchaseRepository.findByIdAndUserId(purchaseId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
        purchaseRepository.delete(purchase);
    }
}
