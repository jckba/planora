package com.planora.backend.expense.application;

import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.common.security.CurrentUser;
import com.planora.backend.expense.application.get.GetExpensesService;
import com.planora.backend.expense.domain.Expense;
import com.planora.backend.expense.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetExpensesServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private GetExpensesService getExpensesService;

    @Test
    void shouldGetExpensesByUserId() {
        UUID userId = UUID.randomUUID();

        PageRequest pageRequest =
            new PageRequest(0, 10);

        Expense expense = Expense.create(
            userId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Lunch",
            null,
            new BigDecimal("25.50"),
            Instant.now()
        );

        PageResult<Expense> expected =
            new PageResult<>(
                List.of(expense),
                0,
                10,
                1
            );

        when(
            expenseRepository.findByUserId(
                userId,
                pageRequest
            )
        ).thenReturn(expected);
        when(currentUser.userId()).thenReturn(userId);

        PageResult<Expense> result =
            getExpensesService.execute(pageRequest);

        assertSame(expected, result);

        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements());
        assertEquals(0, result.page());
        assertEquals(10, result.size());

        verify(expenseRepository)
            .findByUserId(
                userId,
                pageRequest
            );
    }
}
