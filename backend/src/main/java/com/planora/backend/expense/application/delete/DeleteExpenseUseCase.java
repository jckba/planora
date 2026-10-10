package com.planora.backend.expense.application.delete;

import java.util.UUID;

public interface DeleteExpenseUseCase {
    void execute(UUID expenseId);
}
