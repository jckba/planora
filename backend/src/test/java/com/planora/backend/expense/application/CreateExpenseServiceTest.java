package com.planora.backend.expense.application;

import com.planora.backend.account.repository.AccountRepository;
import com.planora.backend.category.repository.CategoryRepository;
import com.planora.backend.expense.domain.Expense;
import com.planora.backend.expense.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private CreateExpenseService createExpenseService;

    @Test
    void shouldCreateExpense() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Instant expenseDate = Instant.now();

        CreateExpenseCommand command = new CreateExpenseCommand(
            accountId,
            categoryId,
            "Lunch",
            "Lunch at restaurant",
            new BigDecimal("25.50"),
            expenseDate
        );

        Expense savedExpense = Expense.create(
            userId,
            accountId,
            categoryId,
            "Lunch",
            "Lunch at restaurant",
            new BigDecimal("25.50"),
            expenseDate
        );

        when(expenseRepository.save(any(Expense.class)))
            .thenReturn(savedExpense);
        Expense result = createExpenseService.execute(userId, command);

        assertSame(savedExpense, result);

        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);

        verify(expenseRepository).save(captor.capture());

        Expense createdExpense = captor.getValue();

        assertEquals(userId, createdExpense.getUserId());
        assertEquals(accountId, createdExpense.getAccountId());
        assertEquals(categoryId, createdExpense.getCategoryId());
        assertEquals("Lunch", createdExpense.getTitle());
        assertEquals("Lunch at restaurant", createdExpense.getDescription());
        assertEquals(0, new BigDecimal("25.50").compareTo(createdExpense.getAmount()));
        assertEquals(expenseDate, createdExpense.getExpenseDate());

        assertNotNull(createdExpense.getId());
        assertNotNull(createdExpense.getCreatedAt());
        assertNotNull(createdExpense.getUpdatedAt());
        assertEquals(0, createdExpense.getVersion());
    }

    @Test
    void shouldRejectInvalidExpense() {
        UUID userId = UUID.randomUUID();
        CreateExpenseCommand command = new CreateExpenseCommand(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Lunch",
            null,
            BigDecimal.ZERO,
            Instant.now()
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> createExpenseService.execute(userId, command)
        );

        verifyNoInteractions(expenseRepository);
    }

}
