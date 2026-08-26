package com.planora.backend.purchase.application.item.add;

import java.math.BigDecimal;
import java.util.UUID;

public record AddPurchaseItemCommand(
    UUID purchaseId,
    UUID userId,
    UUID categoryId,
    String name,
    BigDecimal quantity,
    BigDecimal unitPrice
) {
}
