package com.planora.backend.purchase.application.item.add;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.common.security.CurrentUser;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.domain.PurchaseItem;
import com.planora.backend.purchase.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AddPurchaseItemService implements AddPurchaseItemUseCase {

    private final PurchaseRepository purchaseRepository;
    private final CurrentUser currentUser;

    public AddPurchaseItemService(PurchaseRepository purchaseRepository, CurrentUser currentUser) {
        this.purchaseRepository = purchaseRepository;
        this.currentUser = currentUser;
    }

    @Override
    public Purchase execute(AddPurchaseItemCommand command) {
        UUID userId = currentUser.userId();
        Purchase purchase =
            purchaseRepository
                .findByIdAndUserId(
                    command.purchaseId(),
                    userId
                )
                .orElseThrow(
                    () -> new ResourceNotFoundException(
                        "Purchase not found"
                    )
                );

        PurchaseItem item =
            PurchaseItem.create(
                command.categoryId(),
                command.name(),
                command.quantity(),
                command.unitPrice()
            );

        purchase.addItem(item);

        return purchaseRepository.update(purchase);
    }
}
