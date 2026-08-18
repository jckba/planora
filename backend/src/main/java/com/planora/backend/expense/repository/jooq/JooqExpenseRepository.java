package com.planora.backend.expense.repository.jooq;

import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.expense.domain.Expense;
import com.planora.backend.expense.repository.ExpenseRepository;
import com.planora.persistence.jooq.tables.records.ExpenseRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.planora.persistence.jooq.tables.Expense.EXPENSE;

@Repository
public class JooqExpenseRepository implements ExpenseRepository {

    private final DSLContext dsl;

    public JooqExpenseRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Expense save(Expense expense) {
        dsl.insertInto(EXPENSE)
            .set(EXPENSE.ID, expense.getId())
            .set(EXPENSE.USER_ID, expense.getUserId())
            .set(EXPENSE.ACCOUNT_ID, expense.getAccountId())
            .set(EXPENSE.CATEGORY_ID, expense.getCategoryId())
            .set(EXPENSE.TITLE, expense.getTitle())
            .set(EXPENSE.DESCRIPTION, expense.getDescription())
            .set(EXPENSE.AMOUNT, expense.getAmount())
            .set(EXPENSE.EXPENSE_DATE, OffsetDateTime.ofInstant(expense.getExpenseDate(), ZoneOffset.UTC))
            .set(EXPENSE.CREATED_AT, OffsetDateTime.ofInstant(expense.getCreatedAt(), ZoneOffset.UTC))
            .set(EXPENSE.UPDATED_AT, OffsetDateTime.ofInstant(expense.getUpdatedAt(), ZoneOffset.UTC))
            .set(EXPENSE.VERSION, expense.getVersion())
            .execute();

        return expense;
    }

    @Override
    public Optional<Expense> findById(UUID id) {
        ExpenseRecord record = dsl.selectFrom(EXPENSE)
            .where(EXPENSE.ID.eq(id))
            .fetchOne();
        return Optional.ofNullable(record).map(this::mapToExpense);
    }

    @Override
    public PageResult<Expense> findByUserId(UUID userId, PageRequest pageRequest) {
        int offset = pageRequest.page() * pageRequest.size();

        List<ExpenseRecord> records = dsl.selectFrom(EXPENSE)
            .where(EXPENSE.USER_ID.eq(userId))
            .orderBy(EXPENSE.EXPENSE_DATE.desc(), EXPENSE.ID.desc())
            .limit(pageRequest.size())
            .offset(offset)
            .fetch();

        List<Expense> expenses = records.stream()
            .map(this::mapToExpense)
            .toList();

        long totalElements = dsl.selectCount()
            .from(EXPENSE)
            .where(EXPENSE.USER_ID.eq(userId))
            .fetchOptional(0, Long.class)
            .orElse(0L);

        return new PageResult<>(
            expenses,
            pageRequest.page(),
            pageRequest.size(),
            totalElements
        );

    }

    private Expense mapToExpense(ExpenseRecord record) {
        return Expense.restore(
            record.getId(),
            record.getUserId(),
            record.getAccountId(),
            record.getCategoryId(),
            record.getTitle(),
            record.getDescription(),
            record.getAmount(),
            record.getExpenseDate().toInstant(),
            record.getCreatedAt().toInstant(),
            record.getUpdatedAt().toInstant(),
            record.getVersion()
        );
    }


}
