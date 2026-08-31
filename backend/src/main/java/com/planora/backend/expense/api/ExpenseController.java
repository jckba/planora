package com.planora.backend.expense.api;

import com.planora.backend.common.api.PageResponse;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.expense.api.dto.CreateExpenseRequest;
import com.planora.backend.expense.api.dto.ExpenseResponse;
import com.planora.backend.expense.api.dto.UpdateExpenseRequest;
import com.planora.backend.expense.application.create.CreateExpenseCommand;
import com.planora.backend.expense.application.create.CreateExpenseUseCase;
import com.planora.backend.expense.application.delete.DeleteExpenseUseCase;
import com.planora.backend.expense.application.get.GetExpenseByIdUseCase;
import com.planora.backend.expense.application.get.GetExpensesUseCase;
import com.planora.backend.expense.application.update.UpdateExpenseCommand;
import com.planora.backend.expense.application.update.UpdateExpenseUseCase;
import com.planora.backend.expense.domain.Expense;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final GetExpensesUseCase getExpensesUseCase;
    private final GetExpenseByIdUseCase getExpenseByIdUseCase;
    private final CreateExpenseUseCase createExpenseUseCase;
    private final UpdateExpenseUseCase updateExpenseUseCase;
    private final DeleteExpenseUseCase deleteExpenseUseCase;

    public ExpenseController(GetExpensesUseCase getExpensesUseCase, GetExpenseByIdUseCase getExpenseByIdUseCase, CreateExpenseUseCase createExpenseUseCase, UpdateExpenseUseCase updateExpenseUseCase, DeleteExpenseUseCase deleteExpenseUseCase) {
        this.getExpensesUseCase = getExpensesUseCase;
        this.getExpenseByIdUseCase = getExpenseByIdUseCase;
        this.createExpenseUseCase = createExpenseUseCase;
        this.updateExpenseUseCase = updateExpenseUseCase;
        this.deleteExpenseUseCase = deleteExpenseUseCase;
    }

    @GetMapping
    public PageResponse<ExpenseResponse> getExpenses(
        @RequestParam UUID userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        PageResult<Expense> result = getExpensesUseCase.execute(
            userId,
            new PageRequest(page, size)
        );

        return new PageResponse<>(
            result.content()
                .stream()
                .map(ExpenseResponse::from)
                .toList(),
            result.page(),
            result.size(),
            result.totalElements()
        );
    }

    @GetMapping("/{expenseId}")
    public ExpenseResponse getExpenseById(
        @RequestParam UUID userId,
        @PathVariable UUID expenseId
    ) {
        Expense expense = getExpenseByIdUseCase.execute(userId, expenseId);
        return ExpenseResponse.from(expense);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse createExpense(
        @RequestParam UUID userId,
        @Valid @RequestBody CreateExpenseRequest request
    ) {
        CreateExpenseCommand command = new CreateExpenseCommand(
            request.accountId(),
            request.categoryId(),
            request.title(),
            request.description(),
            request.amount(),
            request.expenseDate()
        );

        Expense expense = createExpenseUseCase.execute(userId, command);

        return ExpenseResponse.from(expense);
    }

    @PutMapping("/{expenseId}")
    public ExpenseResponse updateExpense(
        @RequestParam UUID userId,
        @PathVariable UUID expenseId,
        @Valid @RequestBody UpdateExpenseRequest request
    ) {
        UpdateExpenseCommand command = new UpdateExpenseCommand(
            userId,
            expenseId,
            request.accountId(),
            request.categoryId(),
            request.title(),
            request.description(),
            request.amount(),
            request.expenseDate()
        );

        Expense expense = updateExpenseUseCase.execute(command);

        return ExpenseResponse.from(expense);
    }

    @DeleteMapping("/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(
        @RequestParam UUID userId,
        @PathVariable UUID expenseId
    ) {
        deleteExpenseUseCase.execute(
            userId, expenseId
        );
    }
}
