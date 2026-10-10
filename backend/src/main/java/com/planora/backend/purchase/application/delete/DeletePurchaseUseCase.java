package com.planora.backend.purchase.application.delete;

import java.util.UUID;

public interface DeletePurchaseUseCase {
    void execute(UUID purchaseId);
}
