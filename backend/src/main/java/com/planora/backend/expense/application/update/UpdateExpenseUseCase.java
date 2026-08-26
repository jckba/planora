package com.planora.backend.expense.application.update;

import com.planora.backend.expense.domain.Expense;

public interface UpdateExpenseUseCase {
    Expense execute(UpdateExpenseCommand command);
}
