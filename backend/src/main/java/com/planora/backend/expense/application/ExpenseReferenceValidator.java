package com.planora.backend.expense.application;

import com.planora.backend.account.repository.AccountRepository;
import com.planora.backend.category.repository.CategoryRepository;
import com.planora.backend.common.exception.InvalidExpenseReferenceException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ExpenseReferenceValidator {

    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseReferenceValidator(AccountRepository accountRepository, CategoryRepository categoryRepository) {
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    public void validate(UUID userId, UUID accountId, UUID categoryId) {
        if (accountRepository.findByIdAndUserId(accountId, userId).isEmpty()) {
            throw new InvalidExpenseReferenceException("Account not found");
        }

        if (categoryRepository.findByIdAndUserId(categoryId, userId).isEmpty()) {
            throw new InvalidExpenseReferenceException("Category not found");
        }

    }

}
