package com.planora.backend.category.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    @Test
    void shouldCreateValidCategory() {
        UUID userId = UUID.randomUUID();

        Category category = Category.create(
            userId,
            "Food",
            "#FF0000",
            "restaurant"
        );

        assertNotNull(category);
        assertNotNull(category.getId());

        assertEquals(userId, category.getUserId());
        assertEquals("Food", category.getName());
        assertEquals("#FF0000", category.getColor());
        assertEquals("restaurant", category.getIcon());

        assertNull(category.getDeletedAt());

        assertNotNull(category.getCreatedAt());
        assertNotNull(category.getUpdatedAt());
        assertEquals(0, category.getVersion());
    }

    @Test
    void shouldNormalizeName() {
        Category category = Category.create(
            UUID.randomUUID(),
            "  Food  and    Drinks   ",
            "#FF0000",
            "restaurant"
        );

        assertEquals("Food and Drinks", category.getName());
    }

    @Test
    void shouldNormalizeColor() {
        Category category = Category.create(
            UUID.randomUUID(),
            "Food",
            "  #FF0000  ",
            "restaurant"
        );

        assertEquals(
            "#FF0000",
            category.getColor()
        );
    }

    @Test
    void shouldNormalizeIcon() {
        Category category = Category.create(
            UUID.randomUUID(),
            "Food",
            "#FF0000",
            "  restaurant  "
        );

        assertEquals(
            "restaurant",
            category.getIcon()
        );
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Category.create(
                UUID.randomUUID(),
                " \t ",
                "#FF0000",
                "restaurant"
            )
        );
    }

    @Test
    void shouldRejectBlankColor() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Category.create(
                UUID.randomUUID(),
                "Food",
                " \t ",
                "restaurant"
            )
        );
    }

    @Test
    void shouldRejectBlankIcon() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Category.create(
                UUID.randomUUID(),
                "Food",
                "#FF0000",
                " \t "
            )
        );
    }

    @Test
    void shouldRejectNullUserId() {
        assertThrows(
            NullPointerException.class,
            () -> Category.create(
                null,
                "Food",
                "#FF0000",
                "restaurant"
            )
        );
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(
            NullPointerException.class,
            () -> Category.create(
                UUID.randomUUID(),
                null,
                "#FF0000",
                "restaurant"
            )
        );
    }

    @Test
    void shouldRejectNullColor() {
        assertThrows(
            NullPointerException.class,
            () -> Category.create(
                UUID.randomUUID(),
                "Food",
                null,
                "restaurant"
            )
        );
    }

    @Test
    void shouldRejectNullIcon() {
        assertThrows(
            NullPointerException.class,
            () -> Category.create(
                UUID.randomUUID(),
                "Food",
                "#FF0000",
                null
            )
        );
    }


    @Test
    void shouldRestoreDeletedCategory() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant deletedAt = Instant.now();
        Instant createdAt = deletedAt.minusSeconds(3600);
        Instant updatedAt = deletedAt.minusSeconds(1800);

        Category category = Category.restore(
            id,
            userId,
            "Food",
            "#FF0000",
            "restaurant",
            deletedAt,
            createdAt,
            updatedAt,
            1
        );

        assertEquals(id, category.getId());
        assertEquals(userId, category.getUserId());
        assertEquals("Food", category.getName());
        assertEquals("#FF0000", category.getColor());
        assertEquals("restaurant", category.getIcon());
        assertEquals(deletedAt, category.getDeletedAt());
        assertEquals(createdAt, category.getCreatedAt());
        assertEquals(updatedAt, category.getUpdatedAt());
        assertEquals(1, category.getVersion());
    }

    @Test
    void shouldDeleteCategory() {
        Category category = Category.create(
            UUID.randomUUID(),
            "Food",
            "#FF0000",
            "restaurant"
        );

        Instant previousUpdatedAt = category.getUpdatedAt();
        Integer previousVersion = category.getVersion();

        category.delete();

        assertNotNull(category.getDeletedAt());
        assertTrue(category.getUpdatedAt().compareTo(previousUpdatedAt) >= 0);
        assertEquals(previousVersion + 1, category.getVersion());
    }

    @Test
    void shouldNotChangeAlreadyDeletedCategory() {
        Category category = Category.create(
            UUID.randomUUID(),
            "Food",
            "#FF0000",
            "restaurant"
        );

        category.delete();

        Instant deletedAt = category.getDeletedAt();
        Instant updatedAt = category.getUpdatedAt();
        Integer version = category.getVersion();

        category.delete();

        assertEquals(deletedAt, category.getDeletedAt());
        assertEquals(updatedAt, category.getUpdatedAt());
        assertEquals(version, category.getVersion());
    }

    @Test
    void shouldUpdateCategory() {
        Category category = Category.create(
            UUID.randomUUID(),
            "Food",
            "#FF0000",
            "restaurant"
        );

        Instant previousUpdatedAt = category.getUpdatedAt();
        int previousVersion = category.getVersion();

        category.update(
            "Restaurants",
            "#00FF00",
            "dining"
        );

        assertEquals("Restaurants", category.getName());
        assertEquals("#00FF00", category.getColor());
        assertEquals("dining", category.getIcon());

        assertNotNull(category.getUpdatedAt());
        assertTrue(
            category.getUpdatedAt().compareTo(previousUpdatedAt) >= 0
        );

        assertEquals(previousVersion + 1, category.getVersion());
    }

    @Test
    void shouldNormalizeUpdatedCategoryValues() {
        Category category = Category.create(
            UUID.randomUUID(),
            "Food",
            "#FF0000",
            "restaurant"
        );

        category.update(
            "  Restaurants   and   Bars  ",
            "  #00FF00  ",
            "  dining  "
        );

        assertEquals(
            "Restaurants and Bars",
            category.getName()
        );
        assertEquals("#00FF00", category.getColor());
        assertEquals("dining", category.getIcon());
    }

    @Test
    void shouldRejectBlankUpdatedName() {
        Category category = Category.create(
            UUID.randomUUID(),
            "Food",
            "#FF0000",
            "restaurant"
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> category.update(
                "   ",
                "#00FF00",
                "dining"
            )
        );
    }

    @Test
    void shouldRejectBlankUpdatedColor() {
        Category category = Category.create(
            UUID.randomUUID(),
            "Food",
            "#FF0000",
            "restaurant"
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> category.update(
                "Restaurants",
                "   ",
                "dining"
            )
        );
    }

    @Test
    void shouldRejectBlankUpdatedIcon() {
        Category category = Category.create(
            UUID.randomUUID(),
            "Food",
            "#FF0000",
            "restaurant"
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> category.update(
                "Restaurants",
                "#00FF00",
                "   "
            )
        );
    }

}
