package com.planora.backend.expense.application.get;

import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.expense.domain.Expense;
import com.planora.backend.expense.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetExpensesService implements GetExpensesUseCase{

    private final ExpenseRepository expenseRepository;

    public GetExpensesService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public PageResult<Expense> execute(UUID userId, PageRequest pageRequest) {
        return expenseRepository.findByUserId(userId, pageRequest);
    }
}
