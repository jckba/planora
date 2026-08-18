package com.planora.backend.expense.repository;

import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.expense.domain.Expense;

import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository {

    Expense save(Expense expense);

    Optional<Expense> findById(UUID id);

    PageResult<Expense> findByUserId(UUID userId, PageRequest pageRequest);
}
