package com.planora.backend.purchase.application.item.remove;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

@Service
public class RemovePurchaseItemService implements RemovePurchaseItemUseCase{

    private final PurchaseRepository purchaseRepository;

    public RemovePurchaseItemService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @Override
    public Purchase execute(RemovePurchaseItemCommand command) {
        Purchase purchase = purchaseRepository.findByIdAndUserId(
            command.purchaseId(),
            command.userId()
        ).orElseThrow(
            () -> new ResourceNotFoundException("Purchase not found")
        );
        purchase.removeItem(command.itemId());
        return purchaseRepository.update(purchase);
    }

}
