package com.planora.backend.purchase.application.create;

import java.time.Instant;

public record CreatePurchaseCommand(
    Instant expectedDate,
    String notes
) {
}
