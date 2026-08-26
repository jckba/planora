package com.planora.backend.purchase.application.payment.add;

import java.math.BigDecimal;
import java.util.UUID;

public record AddPurchasePaymentCommand(
    UUID purchaseId,
    UUID userId,
    UUID accountId,
    BigDecimal amount
) {
}
