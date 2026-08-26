package com.planora.backend.purchase.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record AddPurchasePaymentRequest(

    @NotNull
    UUID accountId,

    @NotNull
    @DecimalMin(value = "0.0001")
    BigDecimal amount
) {
}
