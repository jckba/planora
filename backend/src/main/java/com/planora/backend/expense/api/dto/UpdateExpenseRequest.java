package com.planora.backend.expense.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UpdateExpenseRequest(
    UUID accountId,
    UUID categoryId,
    String title,
    String description,
    BigDecimal amount,
    Instant expenseDate
) {
}
