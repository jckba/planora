package com.planora.backend.purchase.application.item.add;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.domain.PurchaseItem;
import com.planora.backend.purchase.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

@Service
public class AddPurchaseItemService implements AddPurchaseItemUseCase {

    private final PurchaseRepository purchaseRepository;

    public AddPurchaseItemService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @Override
    public Purchase execute(AddPurchaseItemCommand command) {
        Purchase purchase =
            purchaseRepository
                .findByIdAndUserId(
                    command.purchaseId(),
                    command.userId()
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
