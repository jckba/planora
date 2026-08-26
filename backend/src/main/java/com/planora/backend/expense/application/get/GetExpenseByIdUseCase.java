package com.planora.backend.expense.application.get;

import com.planora.backend.expense.domain.Expense;

import java.util.UUID;

public interface GetExpenseByIdUseCase {

    Expense execute(UUID userId, UUID expenseId);
}
