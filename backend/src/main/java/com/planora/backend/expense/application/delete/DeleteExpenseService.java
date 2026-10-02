package com.planora.backend.expense.application.delete;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.common.security.CurrentUser;
import com.planora.backend.expense.domain.Expense;
import com.planora.backend.expense.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteExpenseService implements DeleteExpenseUseCase{

    private final ExpenseRepository expenseRepository;
    private final CurrentUser currentUser;

    public DeleteExpenseService(ExpenseRepository expenseRepository, CurrentUser currentUser) {
        this.expenseRepository = expenseRepository;
        this.currentUser = currentUser;
    }

    @Override
    public void execute(UUID expenseId) {
        UUID userId = currentUser.userId();
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        expense.delete();
        expenseRepository.delete(expense);
    }
}
