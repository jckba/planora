package com.planora.backend.expense.application.get;

import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.common.security.CurrentUser;
import com.planora.backend.expense.domain.Expense;
import com.planora.backend.expense.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetExpensesService implements GetExpensesUseCase{

    private final ExpenseRepository expenseRepository;
    private final CurrentUser currentUser;

    public GetExpensesService(ExpenseRepository expenseRepository, CurrentUser currentUser) {
        this.expenseRepository = expenseRepository;
        this.currentUser = currentUser;
    }

    @Override
    public PageResult<Expense> execute(PageRequest pageRequest) {
        UUID userId = currentUser.userId();
        return expenseRepository.findByUserId(userId, pageRequest);
    }
}
