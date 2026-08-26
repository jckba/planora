package com.planora.backend.purchase.api.dto.response;

import com.planora.backend.purchase.domain.PurchaseStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PurchaseResponse(
    UUID id,
    PurchaseStatus status,
    Instant expectedDate,
    Instant purchaseDate,
    BigDecimal total,
    String notes,
    Instant createdAt,
    Instant updatedAt,
    Integer version,
    List<PurchaseItemResponse> items,
    List<PurchasePaymentResponse> payments
) {
}
