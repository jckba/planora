package com.planora.backend.expense.application.update;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.expense.application.ExpenseReferenceValidator;
import com.planora.backend.expense.domain.Expense;
import com.planora.backend.expense.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

@Service
public class UpdateExpenseService implements UpdateExpenseUseCase{

    private final ExpenseRepository expenseRepository;
    private final ExpenseReferenceValidator expenseReferenceValidator;

    public UpdateExpenseService(ExpenseRepository expenseRepository, ExpenseReferenceValidator expenseReferenceValidator) {
        this.expenseRepository = expenseRepository;
        this.expenseReferenceValidator = expenseReferenceValidator;
    }

    @Override
    public Expense execute(UpdateExpenseCommand command) {
        expenseReferenceValidator.validate(command.userId(), command.accountId(), command.categoryId());

        Expense expense = expenseRepository.findByIdAndUserId(
            command.expenseId(), command.userId()
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
