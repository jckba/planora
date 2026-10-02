package com.planora.backend.expense.application;

import com.planora.backend.common.exception.InvalidExpenseReferenceException;
import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.common.security.CurrentUser;
import com.planora.backend.expense.application.update.UpdateExpenseCommand;
import com.planora.backend.expense.application.update.UpdateExpenseService;
import com.planora.backend.expense.domain.Expense;
import com.planora.backend.expense.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseReferenceValidator expenseReferenceValidator;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    UpdateExpenseService updateExpenseService;

    @Test
    void shouldUpdateExpense() {
        UUID userId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Expense expense = Expense.create(
            userId,
            accountId,
            categoryId,
            "Lunch",
            "Lunch at restaurant",
            new BigDecimal("25.50"),
            Instant.parse("2026-08-10T12:00:00Z")
        );

        expense.setId(expenseId);

        UpdateExpenseCommand command = new UpdateExpenseCommand(
            expenseId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Updated Lunch",
            "Updated Lunch at restaurant",
            new BigDecimal("40.00"),
            Instant.parse("2026-08-11T19:00:00Z")
        );

        when(
            expenseRepository.findByIdAndUserId(
                expenseId,
                userId
            )
        ).thenReturn(Optional.of(expense));

        when(
            expenseRepository.update(expense)
        ).thenReturn(expense);
        when(currentUser.userId()).thenReturn(userId);

        Expense result =
            updateExpenseService.execute(command);

        assertSame(expense, result);

        verify(expenseReferenceValidator)
            .validate(
                userId,
                command.accountId(),
                command.categoryId()
            );

        verify(expenseRepository)
            .findByIdAndUserId(
                expenseId,
                userId
            );

        verify(expenseRepository)
            .update(expense);

        assertEquals("Updated Lunch", expense.getTitle());
        assertEquals(command.accountId(), expense.getAccountId());
        assertEquals(command.categoryId(), expense.getCategoryId());
        assertEquals(
            "Updated Lunch at restaurant",
            expense.getDescription()
        );
        assertEquals(
            0,
            new BigDecimal("40.00")
                .compareTo(expense.getAmount())
        );
        assertEquals(
            Instant.parse("2026-08-11T19:00:00Z"),
            expense.getExpenseDate()
        );
    }

    @Test
    void shouldRejectInvalidReferences() {
        UUID userId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        UpdateExpenseCommand command =
            new UpdateExpenseCommand(
                expenseId,
                accountId,
                categoryId,
                "Dinner",
                null,
                new BigDecimal("40.00"),
                Instant.now()
            );

        doThrow(
            new InvalidExpenseReferenceException(
                "Account not found"
            )
        )
            .when(expenseReferenceValidator)
            .validate(
                userId,
                accountId,
                categoryId
            );
        when(currentUser.userId()).thenReturn(userId);

        assertThrows(
            InvalidExpenseReferenceException.class,
            () -> updateExpenseService.execute(command)
        );

        verify(expenseReferenceValidator)
            .validate(
                userId,
                accountId,
                categoryId
            );

        verifyNoInteractions(expenseRepository);
    }

    @Test
    void shouldThrowWhenExpenseDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        UpdateExpenseCommand command =
            new UpdateExpenseCommand(
                expenseId,
                accountId,
                categoryId,
                "Dinner",
                null,
                new BigDecimal("40.00"),
                Instant.now()
            );

        when(
            expenseRepository.findByIdAndUserId(
                expenseId,
                userId
            )
        ).thenReturn(Optional.empty());
        when(currentUser.userId()).thenReturn(userId);

        assertThrows(
            ResourceNotFoundException.class,
            () -> updateExpenseService.execute(command)
        );

        verify(expenseReferenceValidator)
            .validate(
                userId,
                accountId,
                categoryId
            );

        verify(expenseRepository)
            .findByIdAndUserId(
                expenseId,
                userId
            );

        verify(
            expenseRepository,
            never()
        ).update(any(Expense.class));
    }


}
