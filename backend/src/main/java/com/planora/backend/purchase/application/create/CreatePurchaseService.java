package com.planora.backend.purchase.application.create;

import com.planora.backend.common.security.CurrentUser;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreatePurchaseService implements CreatePurchaseUseCase {

    private final PurchaseRepository purchaseRepository;
    private final CurrentUser currentUser;

    public CreatePurchaseService(PurchaseRepository purchaseRepository, CurrentUser currentUser) {
        this.purchaseRepository = purchaseRepository;
        this.currentUser = currentUser;
    }

    @Override
    public Purchase execute(CreatePurchaseCommand command) {
        UUID userId = currentUser.userId();
        Purchase purchase = Purchase.create(
            userId,
            command.expectedDate(),
            command.notes()
        );

        return purchaseRepository.save(purchase);
    }
}
