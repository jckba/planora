package com.planora.backend.expense.application.create;

import com.planora.backend.expense.domain.Expense;

import java.util.UUID;

public interface CreateExpenseUseCase {
    Expense execute(CreateExpenseCommand command);

}
