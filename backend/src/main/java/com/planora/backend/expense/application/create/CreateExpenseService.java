package com.planora.backend.expense.application.create;

import com.planora.backend.expense.application.ExpenseReferenceValidator;
import com.planora.backend.expense.domain.Expense;
import com.planora.backend.expense.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateExpenseService implements CreateExpenseUseCase {

    private final ExpenseRepository expenseRepository;

    private final ExpenseReferenceValidator expenseReferenceValidator;

    public CreateExpenseService(ExpenseRepository expenseRepository, ExpenseReferenceValidator expenseReferenceValidator) {
        this.expenseRepository = expenseRepository;
        this.expenseReferenceValidator = expenseReferenceValidator;
    }

    @Override
    public Expense execute(UUID userId, CreateExpenseCommand command) {

        expenseReferenceValidator.validate(userId, command.accountId(), command.categoryId());

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
