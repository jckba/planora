package com.planora.backend.purchase.application.payment.add;

import com.planora.backend.purchase.domain.Purchase;

public interface AddPurchasePaymentUseCase {
    Purchase execute(AddPurchasePaymentCommand command);
}
