package com.planora.backend.expense.repository.jooq;

import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.expense.domain.Expense;
import com.planora.backend.expense.repository.ExpenseRepository;
import com.planora.persistence.jooq.tables.records.ExpenseRecord;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static com.planora.persistence.jooq.Tables.EXPENSE;
import static com.planora.persistence.jooq.tables.Account.ACCOUNT;
import static com.planora.persistence.jooq.tables.AppUser.APP_USER;
import static com.planora.persistence.jooq.tables.Category.CATEGORY;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JooqExpenseRepositoryTest {
    private final ExpenseRepository expenseRepository;
    private final DSLContext dsl;

    @Autowired
    public JooqExpenseRepositoryTest(ExpenseRepository expenseRepository, DSLContext dsl) {
        this.expenseRepository = expenseRepository;
        this.dsl = dsl;
    }

    private void assertInstantEquals(Instant expected, Instant actual) {
        long differenceNanos = Math.abs(
            ChronoUnit.NANOS.between(expected, actual)
        );

        assertTrue(
            differenceNanos <= 1_000,
            () -> "Expected: " + expected + ", actual: " + actual
        );
    }


    private UUID createUserForTest() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        OffsetDateTime nowOffset = now.atOffset(ZoneOffset.UTC);

        dsl.insertInto(APP_USER)
            .set(APP_USER.ID, userId)
            .set(APP_USER.USERNAME, "testuser-" + userId)
            .set(APP_USER.EMAIL, "test-" + userId + "@planora.test")
            .set(APP_USER.PASSWORD_HASH, "test-password")
            .set(APP_USER.FIRST_NAME, "Test")
            .set(APP_USER.LAST_NAME, "User")
            .set(APP_USER.PRIMARY_CURRENCY_ID, (short) 1)
            .set(APP_USER.PASSWORD_UPDATED_AT, nowOffset)
            .set(APP_USER.CREATED_AT, nowOffset)
            .set(APP_USER.UPDATED_AT, nowOffset)
            .set(APP_USER.VERSION, 0)
            .execute();
        return userId;
    }

    private UUID createAccountForTest(UUID userId) {
        UUID accountId = UUID.randomUUID();

        Instant now = Instant.now();
        OffsetDateTime nowOffset = now.atOffset(ZoneOffset.UTC);

        dsl.insertInto(ACCOUNT)
            .set(ACCOUNT.ID, accountId)
            .set(ACCOUNT.USER_ID, userId)
            .set(ACCOUNT.ACCOUNT_TYPE_ID, (short) 1)
            .set(ACCOUNT.CURRENCY_ID, (short) 1)
            .set(ACCOUNT.NAME, "Test Account")
            .set(ACCOUNT.BALANCE, BigDecimal.ZERO)
            .set(ACCOUNT.CREATED_AT, nowOffset)
            .set(ACCOUNT.UPDATED_AT, nowOffset)
            .set(ACCOUNT.VERSION, 0)
            .execute();
        return accountId;
    }

    private UUID createCategoryForTest(UUID userId) {
        UUID categoryId = UUID.randomUUID();

        Instant now = Instant.now();
        OffsetDateTime nowOffset = now.atOffset(ZoneOffset.UTC);

        dsl.insertInto(CATEGORY)
            .set(CATEGORY.ID, categoryId)
            .set(CATEGORY.USER_ID, userId)
            .set(CATEGORY.NAME, "Food")
            .set(CATEGORY.COLOR, "#FF0000")
            .set(CATEGORY.ICON, "restaurant")
            .set(CATEGORY.CREATED_AT, nowOffset)
            .set(CATEGORY.UPDATED_AT, nowOffset)
            .set(CATEGORY.DELETED_AT, (OffsetDateTime) null)
            .set(CATEGORY.VERSION, 0)
            .execute();
        return categoryId;
    }

    private Expense createExpenseForTest(
        UUID userId,
        UUID accountId,
        UUID categoryId,
        Instant expenseDate
    ) {
        Expense expense = Expense.create(
            userId,
            accountId,
            categoryId,
            "Lunch",
            "Lunch at restaurant",
            new BigDecimal("25.50"),
            expenseDate
        );

        return expenseRepository.save(expense);
    }

    @Transactional
    @Test
    void shouldSaveExpense() {
        UUID userId = createUserForTest();
        UUID accountId = createAccountForTest(userId);
        UUID categoryId = createCategoryForTest(userId);

        Expense expense = createExpenseForTest(
            userId,
            accountId,
            categoryId,
            Instant.now()
        );

        ExpenseRecord record = dsl.selectFrom(EXPENSE)
            .where(EXPENSE.ID.eq(expense.getId()))
            .fetchOne();

        assertNotNull(record);
        assertEquals(expense.getId(), record.getId());
        assertEquals(expense.getUserId(), record.getUserId());
        assertEquals(expense.getAccountId(), record.getAccountId());
        assertEquals(expense.getCategoryId(), record.getCategoryId());
        assertEquals(
            0,
            expense.getAmount().compareTo(record.getAmount())
        );
    }

    @Test
    @Transactional
    void shouldFindExpenseById() {
        UUID userId = createUserForTest();
        UUID accountId = createAccountForTest(userId);
        UUID categoryId = createCategoryForTest(userId);

        Expense expense = createExpenseForTest(
            userId,
            accountId,
            categoryId,
            Instant.now()
        );

        Optional<Expense> result = expenseRepository.findById(expense.getId());

        assertTrue(result.isPresent());

        Expense found = result.orElseThrow();

        assertEquals(expense.getId(), found.getId());
        assertEquals(expense.getUserId(), found.getUserId());
        assertEquals(expense.getAccountId(), found.getAccountId());
        assertEquals(expense.getCategoryId(), found.getCategoryId());
        assertEquals(expense.getTitle(), found.getTitle());
        assertEquals(expense.getDescription(), found.getDescription());
        assertEquals(
            0,
            expense.getAmount().compareTo(found.getAmount())
        );
        assertEquals(expense.getVersion(), found.getVersion());
        assertInstantEquals(
            expense.getExpenseDate(),
            found.getExpenseDate()
        );

        assertInstantEquals(
            expense.getCreatedAt(),
            found.getCreatedAt()
        );

        assertInstantEquals(
            expense.getUpdatedAt(),
            found.getUpdatedAt()
        );
    }

    @Test
    @Transactional
    void shouldReturnEmptyWhenExpenseDoesNotExist() {
        Optional<Expense> result = expenseRepository.findById(UUID.randomUUID());
        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    void shouldFindExpensesByUserIdWithPagination() {
        UUID userId = createUserForTest();
        UUID accountId = createAccountForTest(userId);
        UUID categoryId = createCategoryForTest(userId);

        Expense expense1 = createExpenseForTest(
            userId,
            accountId,
            categoryId,
            Instant.parse("2026-08-01T10:00:00Z")
        );

        Expense expense2 = createExpenseForTest(
            userId,
            accountId,
            categoryId,
            Instant.parse("2026-08-02T10:00:00Z")
        );

        Expense expense3 = createExpenseForTest(
            userId,
            accountId,
            categoryId,
            Instant.parse("2026-08-03T10:00:00Z")
        );

        Expense expense4 = createExpenseForTest(
            userId,
            accountId,
            categoryId,
            Instant.parse("2026-08-04T10:00:00Z")
        );

        UUID otherUserId = createUserForTest();
        UUID otherAccountId = createAccountForTest(otherUserId);
        UUID otherCategoryId = createCategoryForTest(otherUserId);

        createExpenseForTest(
            otherUserId,
            otherAccountId,
            otherCategoryId,
            Instant.parse("2026-08-05T10:00:00Z")
        );

        PageResult<Expense> result = expenseRepository.findByUserId(
            userId,
            new PageRequest(0, 2)
        );

        assertEquals(0, result.page());
        assertEquals(2, result.size());
        assertEquals(4, result.totalElements());
        assertEquals(2, result.content().size());
        assertEquals(expense4.getId(), result.content().get(0).getId());
        assertEquals(expense3.getId(), result.content().get(1).getId());

        PageResult<Expense> secondPage = expenseRepository.findByUserId(
            userId,
            new PageRequest(1, 2)
        );

        assertEquals(1, secondPage.page());
        assertEquals(2, secondPage.size());
        assertEquals(4, secondPage.totalElements());
        assertEquals(2, secondPage.content().size());
        assertEquals(
            expense2.getId(),
            secondPage.content().get(0).getId()
        );
        assertEquals(
            expense1.getId(),
            secondPage.content().get(1).getId()
        );

        PageResult<Expense> emptyPage = expenseRepository.findByUserId(
            userId,
            new PageRequest(2, 2)
        );
        assertTrue(emptyPage.content().isEmpty());
        assertEquals(2, emptyPage.page());
        assertEquals(2, emptyPage.size());
        assertEquals(4, emptyPage.totalElements());
    }

    @Test
    void shouldRejectInvalidPageRequest() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new PageRequest(-1, 20)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> new PageRequest(0, 0)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> new PageRequest(0, -1)
        );
    }



}
