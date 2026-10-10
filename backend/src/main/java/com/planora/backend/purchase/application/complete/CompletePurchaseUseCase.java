package com.planora.backend.purchase.application.complete;

import com.planora.backend.purchase.domain.Purchase;

import java.util.UUID;

public interface CompletePurchaseUseCase {
    Purchase execute(UUID purchaseId);
}
