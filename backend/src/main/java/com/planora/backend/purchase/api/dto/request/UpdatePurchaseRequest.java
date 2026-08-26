package com.planora.backend.purchase.api.dto.request;

import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdatePurchaseRequest(
    Instant expectedDate,
    @Size(max = 500)
    String notes
) {
}
