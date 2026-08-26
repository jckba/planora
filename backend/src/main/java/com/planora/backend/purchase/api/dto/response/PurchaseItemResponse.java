package com.planora.backend.purchase.api.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseItemResponse(
    UUID id,
    UUID categoryId,
    String name,
    BigDecimal quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal
) {
}
