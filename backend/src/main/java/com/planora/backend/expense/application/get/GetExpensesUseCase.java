package com.planora.backend.expense.application.get;

import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.expense.domain.Expense;

import java.util.UUID;

public interface GetExpensesUseCase {

    PageResult<Expense> execute(UUID userId, PageRequest pageRequest);
}
