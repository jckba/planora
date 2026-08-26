package com.planora.backend.purchase.application.payment.remove;

import java.util.UUID;

public record RemovePurchasePaymentCommand(
    UUID purchaseId,
    UUID userId,
    UUID paymentId
) {
}
