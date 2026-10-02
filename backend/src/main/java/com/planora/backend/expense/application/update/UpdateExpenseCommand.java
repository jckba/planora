package com.planora.backend.expense.application.update;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UpdateExpenseCommand(
    UUID expenseId,
    UUID accountId,
    UUID categoryId,
    String title,
    String description,
    BigDecimal amount,
    Instant expenseDate
) {
}
