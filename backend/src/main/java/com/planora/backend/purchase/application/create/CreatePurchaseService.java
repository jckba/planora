package com.planora.backend.purchase.application.create;

import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreatePurchaseService implements CreatePurchaseUseCase {

    private final PurchaseRepository purchaseRepository;

    public CreatePurchaseService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @Override
    public Purchase execute(UUID userId, CreatePurchaseCommand command) {
        Purchase purchase = Purchase.create(
            userId,
            command.expectedDate(),
            command.notes()
        );

        return purchaseRepository.save(purchase);
    }
}
