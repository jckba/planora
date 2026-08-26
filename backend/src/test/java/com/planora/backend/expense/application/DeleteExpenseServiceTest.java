package com.planora.backend.expense.application;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.expense.application.delete.DeleteExpenseService;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private DeleteExpenseService deleteExpenseService;

    @Test
    void shouldDeleteExpense() {
        UUID userId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();

        Expense expense = Expense.create(
            userId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Lunch",
            null,
            new BigDecimal("25.50"),
            Instant.now()
        );

        expense.setId(expenseId);

        when(expenseRepository.findByIdAndUserId(expenseId, userId))
            .thenReturn(Optional.of(expense));

        deleteExpenseService.execute(userId, expenseId);
        assertNotNull(expense.getDeletedAt());

        verify(expenseRepository)
            .findByIdAndUserId(expenseId, userId);

        verify(expenseRepository)
            .delete(expense);
    }

    @Test
    void shouldThrowWhenExpenseDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();

        when(
            expenseRepository.findByIdAndUserId(
                expenseId,
                userId
            )
        ).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> deleteExpenseService.execute(
                userId,
                expenseId
            )
        );

        verify(
            expenseRepository
        ).findByIdAndUserId(
            expenseId,
            userId
        );

        verify(
            expenseRepository,
            never()
        ).delete(any(Expense.class));
    }
}

