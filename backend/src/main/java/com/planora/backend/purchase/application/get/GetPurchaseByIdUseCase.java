package com.planora.backend.purchase.application.get;

import com.planora.backend.purchase.domain.Purchase;

import java.util.UUID;

public interface GetPurchaseByIdUseCase {
    Purchase execute(UUID purchaseId);
}
