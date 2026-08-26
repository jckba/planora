package com.planora.backend.purchase.application.payment.remove;

import com.planora.backend.purchase.domain.Purchase;

public interface RemovePurchasePaymentUseCase {
    Purchase execute(RemovePurchasePaymentCommand command);
}
