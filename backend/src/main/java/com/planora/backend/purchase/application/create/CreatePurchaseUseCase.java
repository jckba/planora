package com.planora.backend.purchase.application.create;

import com.planora.backend.purchase.domain.Purchase;

import java.util.UUID;

public interface CreatePurchaseUseCase {
    Purchase execute(UUID userId, CreatePurchaseCommand command);
}
