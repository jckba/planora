package com.planora.backend.purchase.application.update;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

@Service
public class UpdatePurchaseService implements UpdatePurchaseUseCase {

    private final PurchaseRepository purchaseRepository;

    public UpdatePurchaseService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @Override
    public Purchase execute(UpdatePurchaseCommand command) {
        Purchase purchase = purchaseRepository
            .findByIdAndUserId(
                command.purchaseId(),
                command.userId()
            ).orElseThrow(
                () -> new ResourceNotFoundException("Purchase not found")
            );

        purchase.updateDetails(
            command.expectedDate(),
            command.notes()
        );

        return purchaseRepository.update(purchase);
    }
}
