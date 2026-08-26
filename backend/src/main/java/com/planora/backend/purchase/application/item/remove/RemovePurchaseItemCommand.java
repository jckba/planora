package com.planora.backend.purchase.application.item.remove;

import java.util.UUID;

public record RemovePurchaseItemCommand(
    UUID purchaseId,
    UUID userId,
    UUID itemId
) {
}
