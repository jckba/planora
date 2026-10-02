package com.planora.backend.purchase.application.item.add;

import com.planora.backend.purchase.domain.Purchase;

    public interface AddPurchaseItemUseCase {
        Purchase execute(AddPurchaseItemCommand command);
    }
