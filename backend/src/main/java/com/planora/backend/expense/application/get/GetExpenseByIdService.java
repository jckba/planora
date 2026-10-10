package com.planora.backend.expense.application.get;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.common.security.CurrentUser;
import com.planora.backend.expense.domain.Expense;
import com.planora.backend.expense.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetExpenseByIdService implements GetExpenseByIdUseCase{

    private final ExpenseRepository expenseRepository;
    private final CurrentUser currentUser;

    public GetExpenseByIdService(ExpenseRepository expenseRepository, CurrentUser currentUser) {
        this.expenseRepository = expenseRepository;
        this.currentUser = currentUser;
    }

    @Override
    public Expense execute(UUID expenseId) {
        UUID userId = currentUser.userId();
        return expenseRepository.findByIdAndUserId(expenseId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
    }
}
