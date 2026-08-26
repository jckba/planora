package com.planora.backend.purchase.application.cancel;

import com.planora.backend.purchase.domain.Purchase;

import java.util.UUID;

public interface CancelPurchaseUseCase {
    Purchase execute(UUID userId, UUID purchaseId);
}
