package com.planora.backend.expense.repository.jooq;

import com.planora.backend.common.exception.OptimisticLockException;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.expense.domain.Expense;
import com.planora.backend.expense.repository.ExpenseRepository;
import com.planora.persistence.jooq.tables.records.ExpenseRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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
    public Expense update(Expense expense) {

        int updatedRows = dsl.update(EXPENSE)
            .set(EXPENSE.ACCOUNT_ID, expense.getAccountId())
            .set(EXPENSE.CATEGORY_ID, expense.getCategoryId())
            .set(EXPENSE.TITLE, expense.getTitle())
            .set(EXPENSE.DESCRIPTION, expense.getDescription())
            .set(EXPENSE.AMOUNT, expense.getAmount())
            .set(EXPENSE.EXPENSE_DATE, OffsetDateTime.ofInstant(expense.getExpenseDate(), ZoneOffset.UTC))
            .set(EXPENSE.UPDATED_AT, OffsetDateTime.ofInstant(expense.getUpdatedAt(), ZoneOffset.UTC))
            .set(EXPENSE.VERSION, expense.getVersion() + 1)
            .where(EXPENSE.ID.eq(expense.getId()))
            .and(EXPENSE.VERSION.eq(expense.getVersion()))
            .execute();
        if (updatedRows == 0) {
            throw new OptimisticLockException("Expense was modified by another transaction");
        }


        expense.setVersion(expense.getVersion() + 1);
        return expense;
    }

    @Override
    public void delete(Expense expense) {
        Instant deletedAt = expense.getDeletedAt();

        if (deletedAt == null) {
            throw new IllegalStateException("Expense must be marked as deleted before persistence");
        }

        int updatedRows = dsl.update(EXPENSE)
            .set(EXPENSE.DELETED_AT, OffsetDateTime.ofInstant(deletedAt, ZoneOffset.UTC))
            .set(EXPENSE.UPDATED_AT, OffsetDateTime.ofInstant(expense.getUpdatedAt(), ZoneOffset.UTC))
            .set(EXPENSE.VERSION, expense.getVersion() + 1)
            .where(EXPENSE.ID.eq(expense.getId())
                .and(EXPENSE.USER_ID.eq(expense.getUserId()))
                .and(EXPENSE.DELETED_AT.isNull())
                .and(EXPENSE.VERSION.eq(expense.getVersion())))
                .execute();
        if (updatedRows == 0) {
            throw new OptimisticLockException("Expense was modified by another transaction");
        }
        expense.setVersion(expense.getVersion() + 1);
    }

    @Override
    public Optional<Expense> findById(UUID id) {
        ExpenseRecord record = dsl.selectFrom(EXPENSE)
            .where(EXPENSE.ID.eq(id))
            .and(EXPENSE.DELETED_AT.isNull())
            .fetchOne();
        return Optional.ofNullable(record).map(this::mapToExpense);
    }

    @Override
    public Optional<Expense> findByIdAndUserId(UUID id, UUID userId) {
        ExpenseRecord record = dsl.selectFrom(EXPENSE)
            .where(EXPENSE.ID.eq(id)
                .and(EXPENSE.USER_ID.eq(userId)))
            .and(EXPENSE.DELETED_AT.isNull())
            .fetchOne();

        return Optional.ofNullable(record).map(this::mapToExpense);
    }

    @Override
    public PageResult<Expense> findByUserId(UUID userId, PageRequest pageRequest) {
        int offset = pageRequest.page() * pageRequest.size();

        List<ExpenseRecord> records = dsl.selectFrom(EXPENSE)
            .where(EXPENSE.USER_ID.eq(userId))
            .and(EXPENSE.DELETED_AT.isNull())
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
            .and(EXPENSE.DELETED_AT.isNull())
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
            record.getDeletedAt() != null ? record.getDeletedAt().toInstant() : null,
            record.getVersion()
        );
    }
}
