package com.planora.backend.expense.api;

import com.planora.backend.common.api.GlobalExceptionHandler;
import com.planora.backend.common.exception.InvalidExpenseReferenceException;
import com.planora.backend.common.exception.OptimisticLockException;
import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.expense.application.create.CreateExpenseCommand;
import com.planora.backend.expense.application.create.CreateExpenseUseCase;
import com.planora.backend.expense.application.delete.DeleteExpenseUseCase;
import com.planora.backend.expense.application.get.GetExpenseByIdUseCase;
import com.planora.backend.expense.application.get.GetExpensesUseCase;
import com.planora.backend.expense.application.update.UpdateExpenseCommand;
import com.planora.backend.expense.application.update.UpdateExpenseUseCase;
import com.planora.backend.expense.domain.Expense;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
@Import(GlobalExceptionHandler.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetExpensesUseCase getExpensesUseCase;

    @MockitoBean
    private GetExpenseByIdUseCase getExpenseByIdUseCase;

    @MockitoBean
    private CreateExpenseUseCase createExpenseUseCase;

    @MockitoBean
    private UpdateExpenseUseCase updateExpenseUseCase;

    @MockitoBean
    private DeleteExpenseUseCase deleteExpenseUseCase;

    @Test
    void shouldGetExpenses() throws Exception {
        UUID userId = UUID.randomUUID();

        Expense expense = Expense.create(
            userId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Lunch",
            "Lunch at restaurant",
            new BigDecimal("25.50"),
            Instant.parse("2026-08-10T12:00:00Z")
        );

        PageRequest pageRequest =
            new PageRequest(0, 10);

        PageResult<Expense> pageResult =
            new PageResult<>(
                List.of(expense),
                0,
                10,
                1
            );

        when(
            getExpensesUseCase.execute(
                userId,
                pageRequest
            )
        ).thenReturn(pageResult);

        mockMvc.perform(
                get("/api/expenses")
                    .param("userId", userId.toString())
                    .param("page", "0")
                    .param("size", "10")
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.content.length()")
                    .value(1)
            )
            .andExpect(
                jsonPath("$.content[0].id")
                    .value(expense.getId().toString())
            )
            .andExpect(
                jsonPath("$.content[0].accountId")
                    .value(expense.getAccountId().toString())
            )
            .andExpect(
                jsonPath("$.content[0].categoryId")
                    .value(expense.getCategoryId().toString())
            )
            .andExpect(
                jsonPath("$.content[0].title")
                    .value("Lunch")
            )
            .andExpect(
                jsonPath("$.content[0].description")
                    .value("Lunch at restaurant")
            )
            .andExpect(
                jsonPath("$.content[0].amount")
                    .value(25.50)
            )
            .andExpect(
                jsonPath("$.page")
                    .value(0)
            )
            .andExpect(
                jsonPath("$.size")
                    .value(10)
            )
            .andExpect(
                jsonPath("$.totalElements")
                    .value(1)
            );

        verify(getExpensesUseCase)
            .execute(userId, pageRequest);
    }

    @Test
    void shouldGetExpenseById() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();

        Expense expense = Expense.create(
            userId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Lunch",
            "Lunch at restaurant",
            new BigDecimal("25.50"),
            Instant.parse("2026-08-10T12:00:00Z")
        );

        expense.setId(expenseId);

        when(
            getExpenseByIdUseCase.execute(
                userId,
                expenseId
            )
        ).thenReturn(expense);

        mockMvc.perform(
                get(
                    "/api/expenses/{expenseId}",
                    expenseId
                )
                    .param("userId", userId.toString())
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.id")
                    .value(expenseId.toString())
            )
            .andExpect(
                jsonPath("$.accountId")
                    .value(expense.getAccountId().toString())
            )
            .andExpect(
                jsonPath("$.categoryId")
                    .value(expense.getCategoryId().toString())
            )
            .andExpect(
                jsonPath("$.title")
                    .value("Lunch")
            )
            .andExpect(
                jsonPath("$.amount")
                    .value(25.50)
            );

        verify(getExpenseByIdUseCase)
            .execute(userId, expenseId);
    }

    @Test
    void shouldReturn404WhenExpenseDoesNotExist() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();

        when(
            getExpenseByIdUseCase.execute(
                userId,
                expenseId
            )
        ).thenThrow(
            new ResourceNotFoundException(
                "Expense not found"
            )
        );

        mockMvc.perform(
                get(
                    "/api/expenses/{expenseId}",
                    expenseId
                )
                    .param("userId", userId.toString())
            )
            .andExpect(status().isNotFound())
            .andExpect(
                jsonPath("$.code")
                    .value("RESOURCE_NOT_FOUND")
            )
            .andExpect(
                jsonPath("$.message")
                    .value("Expense not found")
            );

        verify(getExpenseByIdUseCase)
            .execute(userId, expenseId);
    }

    @Test
    void shouldCreateExpense() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Instant expenseDate =
            Instant.parse("2026-08-10T12:00:00Z");

        Expense expense = Expense.create(
            userId,
            accountId,
            categoryId,
            "Lunch",
            "Lunch at restaurant",
            new BigDecimal("25.50"),
            expenseDate
        );

        CreateExpenseCommand command =
            new CreateExpenseCommand(
                accountId,
                categoryId,
                "Lunch",
                "Lunch at restaurant",
                new BigDecimal("25.50"),
                expenseDate
            );

        when(
            createExpenseUseCase.execute(
                userId,
                command
            )
        ).thenReturn(expense);

        mockMvc.perform(
                post("/api/expenses")
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                    {
                        "accountId": "%s",
                        "categoryId": "%s",
                        "title": "Lunch",
                        "description": "Lunch at restaurant",
                        "amount": 25.50,
                        "expenseDate": "2026-08-10T12:00:00Z"
                    }
                    """.formatted(
                        accountId,
                        categoryId
                    ))
            )
            .andExpect(status().isCreated())
            .andExpect(
                jsonPath("$.id")
                    .value(expense.getId().toString())
            )
            .andExpect(
                jsonPath("$.accountId")
                    .value(accountId.toString())
            )
            .andExpect(
                jsonPath("$.categoryId")
                    .value(categoryId.toString())
            )
            .andExpect(
                jsonPath("$.title")
                    .value("Lunch")
            )
            .andExpect(
                jsonPath("$.description")
                    .value("Lunch at restaurant")
            )
            .andExpect(
                jsonPath("$.amount")
                    .value(25.50)
            )
            .andExpect(
                jsonPath("$.expenseDate")
                    .value("2026-08-10T12:00:00Z")
            );

        verify(createExpenseUseCase)
            .execute(userId, command);
    }

    @Test
    void shouldReturn404WhenExpenseReferenceDoesNotExist()
        throws Exception {

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        CreateExpenseCommand command =
            new CreateExpenseCommand(
                accountId,
                categoryId,
                "Lunch",
                null,
                new BigDecimal("25.50"),
                Instant.parse("2026-08-10T12:00:00Z")
            );

        when(
            createExpenseUseCase.execute(
                userId,
                command
            )
        ).thenThrow(
            new InvalidExpenseReferenceException(
                "Account not found"
            )
        );

        mockMvc.perform(
                post("/api/expenses")
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                    {
                        "accountId": "%s",
                        "categoryId": "%s",
                        "title": "Lunch",
                        "description": null,
                        "amount": 25.50,
                        "expenseDate": "2026-08-10T12:00:00Z"
                    }
                    """.formatted(
                        accountId,
                        categoryId
                    ))
            )
            .andExpect(status().isNotFound())
            .andExpect(
                jsonPath("$.code")
                    .value("INVALID_EXPENSE_REFERENCE")
            )
            .andExpect(
                jsonPath("$.message")
                    .value("Account not found")
            );

        verify(createExpenseUseCase)
            .execute(userId, command);
    }

    @Test
    void shouldUpdateExpense() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Instant expenseDate =
            Instant.parse("2026-08-11T19:00:00Z");

        Expense expense = Expense.create(
            userId,
            accountId,
            categoryId,
            "Dinner",
            "Dinner at restaurant",
            new BigDecimal("40.00"),
            expenseDate
        );

        expense.setId(expenseId);

        UpdateExpenseCommand command =
            new UpdateExpenseCommand(
                userId,
                expenseId,
                accountId,
                categoryId,
                "Dinner",
                "Dinner at restaurant",
                new BigDecimal("40.00"),
                expenseDate
            );

        when(
            updateExpenseUseCase.execute(command)
        ).thenReturn(expense);

        mockMvc.perform(
                put("/api/expenses/{expenseId}", expenseId)
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                    {
                        "accountId": "%s",
                        "categoryId": "%s",
                        "title": "Dinner",
                        "description": "Dinner at restaurant",
                        "amount": 40.00,
                        "expenseDate": "2026-08-11T19:00:00Z"
                    }
                    """.formatted(
                        accountId,
                        categoryId
                    ))
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.id")
                    .value(expenseId.toString())
            )
            .andExpect(
                jsonPath("$.accountId")
                    .value(accountId.toString())
            )
            .andExpect(
                jsonPath("$.categoryId")
                    .value(categoryId.toString())
            )
            .andExpect(
                jsonPath("$.title")
                    .value("Dinner")
            )
            .andExpect(
                jsonPath("$.description")
                    .value("Dinner at restaurant")
            )
            .andExpect(
                jsonPath("$.amount")
                    .value(40.00)
            )
            .andExpect(
                jsonPath("$.expenseDate")
                    .value("2026-08-11T19:00:00Z")
            );

        verify(updateExpenseUseCase)
            .execute(command);
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingExpense()
        throws Exception {

        UUID userId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        UpdateExpenseCommand command =
            new UpdateExpenseCommand(
                userId,
                expenseId,
                accountId,
                categoryId,
                "Dinner",
                null,
                new BigDecimal("40.00"),
                Instant.parse("2026-08-11T19:00:00Z")
            );

        when(
            updateExpenseUseCase.execute(command)
        ).thenThrow(
            new ResourceNotFoundException(
                "Expense not found"
            )
        );

        mockMvc.perform(
                put("/api/expenses/{expenseId}", expenseId)
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                    {
                        "accountId": "%s",
                        "categoryId": "%s",
                        "title": "Dinner",
                        "description": null,
                        "amount": 40.00,
                        "expenseDate": "2026-08-11T19:00:00Z"
                    }
                    """.formatted(
                        accountId,
                        categoryId
                    ))
            )
            .andExpect(status().isNotFound())
            .andExpect(
                jsonPath("$.code")
                    .value("RESOURCE_NOT_FOUND")
            )
            .andExpect(
                jsonPath("$.message")
                    .value("Expense not found")
            );

        verify(updateExpenseUseCase)
            .execute(command);

    }

    @Test
    void shouldReturn404WhenUpdatingWithInvalidReference()
        throws Exception {

        UUID userId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        UpdateExpenseCommand command =
            new UpdateExpenseCommand(
                userId,
                expenseId,
                accountId,
                categoryId,
                "Dinner",
                null,
                new BigDecimal("40.00"),
                Instant.parse("2026-08-11T19:00:00Z")
            );

        when(
            updateExpenseUseCase.execute(command)
        ).thenThrow(
            new InvalidExpenseReferenceException(
                "Account not found"
            )
        );

        mockMvc.perform(
                put("/api/expenses/{expenseId}", expenseId)
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                    {
                        "accountId": "%s",
                        "categoryId": "%s",
                        "title": "Dinner",
                        "description": null,
                        "amount": 40.00,
                        "expenseDate": "2026-08-11T19:00:00Z"
                    }
                    """.formatted(
                        accountId,
                        categoryId
                    ))
            )
            .andExpect(status().isNotFound())
            .andExpect(
                jsonPath("$.code")
                    .value("INVALID_EXPENSE_REFERENCE")
            )
            .andExpect(
                jsonPath("$.message")
                    .value("Account not found")
            );

        verify(updateExpenseUseCase)
            .execute(command);
    }
    @Test
    void shouldReturn409WhenExpenseWasModifiedConcurrently()
        throws Exception {

        UUID userId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        UpdateExpenseCommand command =
            new UpdateExpenseCommand(
                userId,
                expenseId,
                accountId,
                categoryId,
                "Dinner",
                null,
                new BigDecimal("40.00"),
                Instant.parse("2026-08-11T19:00:00Z")
            );

        when(
            updateExpenseUseCase.execute(command)
        ).thenThrow(
            new OptimisticLockException(
                "Expense was modified by another transaction"
            )
        );

        mockMvc.perform(
                put("/api/expenses/{expenseId}", expenseId)
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                    {
                        "accountId": "%s",
                        "categoryId": "%s",
                        "title": "Dinner",
                        "description": null,
                        "amount": 40.00,
                        "expenseDate": "2026-08-11T19:00:00Z"
                    }
                    """.formatted(
                        accountId,
                        categoryId
                    ))
            )
            .andExpect(status().isConflict())
            .andExpect(
                jsonPath("$.code")
                    .value("OPTIMISTIC_LOCK")
            )
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "Expense was modified by another transaction"
                    )
            );

        verify(updateExpenseUseCase)
            .execute(command);
    }

    @Test
    void shouldDeleteExpense() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();

        doNothing()
            .when(deleteExpenseUseCase)
            .execute(userId, expenseId);

        mockMvc.perform(
                delete(
                    "/api/expenses/{expenseId}",
                    expenseId
                )
                    .param(
                        "userId",
                        userId.toString()
                    )
            )
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        verify(deleteExpenseUseCase)
            .execute(userId, expenseId);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingExpense()
        throws Exception {

        UUID userId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();

        doThrow(
            new ResourceNotFoundException(
                "Expense not found"
            )
        )
            .when(deleteExpenseUseCase)
            .execute(userId, expenseId);

        mockMvc.perform(
                delete(
                    "/api/expenses/{expenseId}",
                    expenseId
                )
                    .param(
                        "userId",
                        userId.toString()
                    )
            )
            .andExpect(status().isNotFound())
            .andExpect(
                jsonPath("$.code")
                    .value("RESOURCE_NOT_FOUND")
            )
            .andExpect(
                jsonPath("$.message")
                    .value("Expense not found")
            );

        verify(deleteExpenseUseCase)
            .execute(userId, expenseId);
    }

    @Test
    void shouldReturn409WhenDeletingExpenseWithStaleVersion()
        throws Exception {

        UUID userId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();

        doThrow(
            new OptimisticLockException(
                "Expense was modified by another transaction"
            )
        )
            .when(deleteExpenseUseCase)
            .execute(userId, expenseId);

        mockMvc.perform(
                delete(
                    "/api/expenses/{expenseId}",
                    expenseId
                )
                    .param(
                        "userId",
                        userId.toString()
                    )
            )
            .andExpect(status().isConflict())
            .andExpect(
                jsonPath("$.code")
                    .value("OPTIMISTIC_LOCK")
            )
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "Expense was modified by another transaction"
                    )
            );

        verify(deleteExpenseUseCase)
            .execute(userId, expenseId);
    }



}
