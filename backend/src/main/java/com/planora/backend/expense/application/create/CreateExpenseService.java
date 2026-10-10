package com.planora.backend.expense.application.create;

import com.planora.backend.common.security.CurrentUser;
import com.planora.backend.expense.application.ExpenseReferenceValidator;
import com.planora.backend.expense.domain.Expense;
import com.planora.backend.expense.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateExpenseService implements CreateExpenseUseCase {

    private final ExpenseRepository expenseRepository;
    private final CurrentUser currentUser;

    private final ExpenseReferenceValidator expenseReferenceValidator;

    public CreateExpenseService(ExpenseRepository expenseRepository, CurrentUser currentUser, ExpenseReferenceValidator expenseReferenceValidator) {
        this.expenseRepository = expenseRepository;
        this.currentUser = currentUser;
        this.expenseReferenceValidator = expenseReferenceValidator;
    }

    @Override
    public Expense execute(CreateExpenseCommand command) {
        UUID userId = currentUser.userId();
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
