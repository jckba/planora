package com.planora.backend.purchase.application.item.remove;

import com.planora.backend.purchase.domain.Purchase;

public interface RemovePurchaseItemUseCase {
    Purchase execute(RemovePurchaseItemCommand command);
}
