package com.planora.backend.purchase.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record AddPurchaseItemRequest(

    @NotNull
    UUID categoryId,

    @NotBlank
    @Size(max = 150)
    String name,

    @NotNull
    @DecimalMin(value = "0.0001")
    BigDecimal quantity,

    @NotNull
    @DecimalMin(value = "0.0")
    BigDecimal unitPrice
) {
}
