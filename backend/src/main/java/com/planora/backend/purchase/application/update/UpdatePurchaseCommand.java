package com.planora.backend.purchase.application.update;

import java.time.Instant;
import java.util.UUID;

public record UpdatePurchaseCommand(
    UUID purchaseId,
    Instant expectedDate,
    String notes
) {
}
