package com.planora.backend.purchase.application.complete;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CompletePurchaseService implements CompletePurchaseUseCase {

    private final PurchaseRepository purchaseRepository;

    public CompletePurchaseService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @Override
    public Purchase execute(UUID userId, UUID purchaseId) {
        Purchase purchase =
            purchaseRepository
                .findByIdAndUserId(
                    purchaseId,
                    userId
                )
                .orElseThrow(
                    () -> new ResourceNotFoundException(
                        "Purchase not found"
                    )
                );

        purchase.complete();

        return purchaseRepository.update(
            purchase
        );
    }
}
