package com.planora.backend.expense.application;

import com.planora.backend.expense.domain.Expense;

import java.util.UUID;

public interface CreateExpenseUseCase {

    Expense execute(UUID userId, CreateExpenseCommand command);

}
