package com.planora.backend.expense.api.dto;

import com.planora.backend.expense.domain.Expense;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ExpenseResponse(
    UUID id,
    UUID accountId,
    UUID categoryId,
    String title,
    String description,
    BigDecimal amount,
    Instant expenseDate
) {
    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(
            expense.getId(),
            expense.getAccountId(),
            expense.getCategoryId(),
            expense.getTitle(),
            expense.getDescription(),
            expense.getAmount(),
            expense.getExpenseDate()
        );
    }

}
