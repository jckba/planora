package com.planora.backend.expense.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateExpenseCommand(
    UUID accountId,
    UUID categoryId,
    String title,
    String description,
    BigDecimal amount,
    Instant expenseDate
) {

}
