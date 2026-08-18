package com.planora.backend.expense.application;

import com.planora.backend.expense.domain.Expense;
import com.planora.backend.expense.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateExpenseService implements CreateExpenseUseCase {

    private final ExpenseRepository expenseRepository;

    public CreateExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public Expense execute(UUID userId, CreateExpenseCommand command) {
        Expense expense = Expense.create(
            userId,
            command.accountId(),
            command.categoryId(),
            command.title(),
            command.description(),
            command.amount(),
            command.expenseDate()
        );
        return expenseRepository.save(expense);
    }

}
