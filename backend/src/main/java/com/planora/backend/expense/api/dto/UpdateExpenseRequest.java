package com.planora.backend.expense.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UpdateExpenseRequest(
    @NotNull
    UUID accountId,
    @NotNull
    UUID categoryId,
    @NotBlank
    @Size(max = 150)
    String title,
    @Size(max = 500)
    String description,
    @NotNull
    @DecimalMin(value = "0.0001")
    BigDecimal amount,
    @NotNull
    Instant expenseDate
) {
}
