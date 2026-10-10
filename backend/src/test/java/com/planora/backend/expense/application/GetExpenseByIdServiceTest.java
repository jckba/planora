package com.planora.backend.expense.application;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.common.security.CurrentUser;
import com.planora.backend.expense.application.get.GetExpenseByIdService;
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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetExpenseByIdServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private GetExpenseByIdService getExpenseByIdService;

    @Test
    void shouldGetExpenseById() {
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

        when(
            expenseRepository.findByIdAndUserId(expenseId, userId))
            .thenReturn(Optional.of(expense));
        when(currentUser.userId()).thenReturn(userId);
        Expense result = getExpenseByIdService.execute(expenseId);
        assertSame(expense, result);
        verify(expenseRepository)
            .findByIdAndUserId(expenseId, userId);
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
        when(currentUser.userId()).thenReturn(userId);

        assertThrows(
            ResourceNotFoundException.class,
            () -> getExpenseByIdService.execute(expenseId)
        );

        verify(expenseRepository)
            .findByIdAndUserId(
                expenseId,
                userId
            );
    }



}
