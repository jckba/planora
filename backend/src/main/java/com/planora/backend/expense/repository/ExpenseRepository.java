package com.planora.backend.expense.repository;

import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.expense.domain.Expense;

import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository {

    Expense save(Expense expense);

    Expense update(Expense expense);

    void delete(Expense expense);

    Optional<Expense> findByIdAndUserId(UUID id, UUID userId);

    PageResult<Expense> findByUserId(UUID userId, PageRequest pageRequest);
}
