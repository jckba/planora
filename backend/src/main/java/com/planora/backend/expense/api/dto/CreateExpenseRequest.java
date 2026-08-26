package com.planora.backend.expense.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateExpenseRequest(
    UUID accountId,
    UUID categoryId,
    String title,
    String description,
    BigDecimal amount,
    Instant expenseDate
) {
}
