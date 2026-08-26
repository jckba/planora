package com.planora.backend.expense.application;


import com.planora.backend.account.domain.Account;
import com.planora.backend.account.repository.AccountRepository;
import com.planora.backend.category.domain.Category;
import com.planora.backend.category.repository.CategoryRepository;
import com.planora.backend.common.exception.InvalidExpenseReferenceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseReferenceValidatorTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ExpenseReferenceValidator validator;

    @Test
    void shouldAcceptValidReferences() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(
            accountRepository.findByIdAndUserId(
                accountId,
                userId
            )
        ).thenReturn(Optional.of(mock(Account.class)));

        when(
            categoryRepository.findByIdAndUserId(
                categoryId,
                userId
            )
        ).thenReturn(Optional.of(mock(Category.class)));

        assertDoesNotThrow(
            () -> validator.validate(
                userId,
                accountId,
                categoryId
            )
        );

        verify(accountRepository)
            .findByIdAndUserId(
                accountId,
                userId
            );

        verify(categoryRepository)
            .findByIdAndUserId(
                categoryId,
                userId
            );
    }

    @Test
    void shouldRejectInvalidAccount() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(
            accountRepository.findByIdAndUserId(
                accountId,
                userId
            )
        ).thenReturn(Optional.empty());

        assertThrows(
            InvalidExpenseReferenceException.class,
            () -> validator.validate(
                userId,
                accountId,
                categoryId
            )
        );

        verify(accountRepository)
            .findByIdAndUserId(
                accountId,
                userId
            );

        verify(categoryRepository, never())
            .findByIdAndUserId(
                categoryId,
                userId
            );
    }

    @Test
    void shouldRejectInvalidCategory() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(
            accountRepository.findByIdAndUserId(
                accountId,
                userId
            )
        ).thenReturn(Optional.of(mock(Account.class)));

        when(
            categoryRepository.findByIdAndUserId(
                categoryId,
                userId
            )
        ).thenReturn(Optional.empty());

        assertThrows(
            InvalidExpenseReferenceException.class,
            () -> validator.validate(
                userId,
                accountId,
                categoryId
            )
        );

        verify(accountRepository)
            .findByIdAndUserId(
                accountId,
                userId
            );

        verify(categoryRepository)
            .findByIdAndUserId(
                categoryId,
                userId
            );
    }
}
