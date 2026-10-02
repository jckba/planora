package com.planora.backend.expense.application.update;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.common.security.CurrentUser;
import com.planora.backend.expense.application.ExpenseReferenceValidator;
import com.planora.backend.expense.domain.Expense;
import com.planora.backend.expense.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateExpenseService implements UpdateExpenseUseCase{

    private final ExpenseRepository expenseRepository;
    private final ExpenseReferenceValidator expenseReferenceValidator;
    private final CurrentUser currentUser;

    public UpdateExpenseService(ExpenseRepository expenseRepository, ExpenseReferenceValidator expenseReferenceValidator, CurrentUser currentUser) {
        this.expenseRepository = expenseRepository;
        this.expenseReferenceValidator = expenseReferenceValidator;
        this.currentUser = currentUser;
    }

    @Override
    public Expense execute(UpdateExpenseCommand command) {
        UUID userId = currentUser.userId();

        expenseReferenceValidator.validate(userId, command.accountId(), command.categoryId());

        Expense expense = expenseRepository.findByIdAndUserId(
            command.expenseId(), userId
        ).orElseThrow(
            () -> new ResourceNotFoundException("Expense not found")
        );

        expense.update(
            command.accountId(),
            command.categoryId(),
            command.title(),
            command.description(),
            command.amount(),
            command.expenseDate()
        );

        return expenseRepository.update(expense);

    }
}
