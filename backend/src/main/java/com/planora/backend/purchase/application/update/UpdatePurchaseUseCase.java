package com.planora.backend.purchase.application.update;

import com.planora.backend.purchase.domain.Purchase;

public interface UpdatePurchaseUseCase {
    Purchase execute(UpdatePurchaseCommand command);
}
