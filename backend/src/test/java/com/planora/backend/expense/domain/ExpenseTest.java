package com.planora.backend.expense.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ExpenseTest {

    // Create test

    @Test
    void shouldCreateValidExpense() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Instant expenseDate = Instant.now();

        Expense expense = Expense.create(
            userId,
            accountId,
            categoryId,
            "Lunch",
            "Lunch at restaurant",
            new BigDecimal("25.50"),
            expenseDate
        );

        assertNotNull(expense);
        assertNotNull(expense.getId());

        assertEquals(userId, expense.getUserId());
        assertEquals(accountId, expense.getAccountId());
        assertEquals(categoryId, expense.getCategoryId());

        assertEquals("Lunch", expense.getTitle());
        assertEquals("Lunch at restaurant", expense.getDescription());
        assertEquals(0, new BigDecimal("25.50").compareTo(expense.getAmount())
        );
        assertEquals(expenseDate, expense.getExpenseDate());

        assertNotNull(expense.getCreatedAt());
        assertNotNull(expense.getUpdatedAt());
        assertEquals(0, expense.getVersion());
    }

    // Normalization tests

    @Test
    void shouldNormalizeTitleWhitespace() {
        Expense expense = Expense.create(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "  Lunch   at    restaurant  ",
            null,
            new BigDecimal("25.50"),
            Instant.now()
        );

        assertEquals(
            "Lunch at restaurant",
            expense.getTitle()
        );
    }
    @Test
    void shouldNormalizeDescriptionWhitespace() {
        Expense expense = Expense.create(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Lunch",
            "  Lunch   at    restaurant  ",
            new BigDecimal("25.50"),
            Instant.now()
        );

        assertEquals(
            "Lunch at restaurant",
            expense.getDescription()
        );
    }

    @Test
    void shouldNormalizeWhitespaceCharacters() {
        Expense expense = Expense.create(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "  Lunch\t\tat\nrestaurant  ",
            "  Meal\t\tat\nrestaurant  ",
            new BigDecimal("25.50"),
            Instant.now()
        );

        assertEquals("Lunch at restaurant", expense.getTitle());
        assertEquals("Meal at restaurant", expense.getDescription());
    }

    @Test
    void shouldConvertBlankDescriptionToNull() {
        Expense expense = Expense.create(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Title",
            " \t  ",
            new BigDecimal("25.50"),
            Instant.now()
        );

        assertNull(expense.getDescription());
    }

    // Title validation tests

    @Test
    void shouldRejectBlankTitle() {
        assertThrows(IllegalArgumentException.class, () -> Expense.create(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            " \t ",
            "Description",
            new BigDecimal("25.50"),
            Instant.now()
        ));
    }

    @Test
    void shouldRejectNullTitle() {
        assertThrows(NullPointerException.class, () -> Expense.create(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            null,
            new BigDecimal("25.50"),
            Instant.now()
        ));
    }

    // Amount validation tests

    @Test
    void shouldRejectNullAmount() {
        assertThrows(NullPointerException.class, () -> Expense.create(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Lunch",
            null,
            null,
            Instant.now()
        ));
    }

    @Test
    void shouldRejectZeroAmount() {
        assertThrows(IllegalArgumentException.class, () -> Expense.create(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Lunch",
            null,
            BigDecimal.ZERO,
            Instant.now()
        ));
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> Expense.create(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Lunch",
            null,
            new BigDecimal("-10.00"),
            Instant.now()
        ));
    }

    // Mandatory references validation tests

    @Test
    void shouldRejectNullUserId() {
        assertThrows(
            NullPointerException.class,
            () -> Expense.create(
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Lunch",
                null,
                new BigDecimal("25.50"),
                Instant.now()
            )
        );
    }

    @Test
    void shouldRejectNullAccountId() {
        assertThrows(
            NullPointerException.class,
            () -> Expense.create(
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                "Lunch",
                null,
                new BigDecimal("25.50"),
                Instant.now()
            )
        );
    }

    @Test
    void shouldRejectNullCategoryId() {
        assertThrows(
            NullPointerException.class,
            () -> Expense.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "Lunch",
                null,
                new BigDecimal("25.50"),
                Instant.now()
            )
        );
    }

    @Test
    void shouldRejectNullExpenseDate() {
        assertThrows(
            NullPointerException.class,
            () -> Expense.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Lunch",
                null,
                new BigDecimal("25.50"),
                null
            )
        );
    }

}
