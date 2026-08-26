package com.planora.backend.expense.application.get;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.expense.domain.Expense;
import com.planora.backend.expense.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetExpenseByIdService implements GetExpenseByIdUseCase{

    private final ExpenseRepository expenseRepository;

    public GetExpenseByIdService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public Expense execute(UUID userId, UUID expenseId) {
        return expenseRepository.findByIdAndUserId(expenseId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
    }
}
