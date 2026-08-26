package com.planora.backend.purchase.api.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchasePaymentResponse(
    UUID id,
    UUID accountId,
    BigDecimal amount
) {
}
