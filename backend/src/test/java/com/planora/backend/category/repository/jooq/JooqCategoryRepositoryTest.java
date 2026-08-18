package com.planora.backend.category.repository.jooq;

import com.planora.backend.category.domain.Category;
import com.planora.backend.category.repository.CategoryRepository;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.persistence.jooq.tables.records.CategoryRecord;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static com.planora.persistence.jooq.tables.AppUser.APP_USER;
import static com.planora.persistence.jooq.tables.Category.CATEGORY;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JooqCategoryRepositoryTest {
    private final CategoryRepository categoryRepository;
    private final DSLContext dsl;

    @Autowired
    public JooqCategoryRepositoryTest(CategoryRepository categoryRepository, DSLContext dsl) {
        this.categoryRepository = categoryRepository;
        this.dsl = dsl;
    }

    private UUID createUserForTest() {
        UUID userId = UUID.randomUUID();

        Instant now = Instant.now();
        OffsetDateTime nowOffset = now.atOffset(ZoneOffset.UTC);

        dsl.insertInto(APP_USER)
            .set(APP_USER.ID, userId)
            .set(APP_USER.USERNAME, "test user-" + UUID.randomUUID())
            .set(APP_USER.EMAIL, "test-" + UUID.randomUUID() + "@planora.test")
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

    private void assertInstantEquals(
        Instant expected,
        Instant actual
    ) {
        long difference = Math.abs(
            ChronoUnit.NANOS.between(expected, actual)
        );

        assertTrue(
            difference <= 1_000,
            "Expected: " + expected + " but was: " + actual
        );
    }

    private Category createCategoryForTest() {
        UUID userId = createUserForTest();

        Category category = Category.create(
            userId,
            "Food",
            "#FF0000",
            "restaurant"
        );

        return categoryRepository.save(category);
    }

    @Test
    @Transactional
    void shouldSaveCategory() {
        UUID userId = createUserForTest();

        Category category = Category.create(
            userId,
            "Food",
            "#FF0000",
            "restaurant"
        );

        Category savedCategory = categoryRepository.save(category);

        CategoryRecord record = dsl.selectFrom(CATEGORY)
            .where(CATEGORY.ID.eq(savedCategory.getId()))
            .fetchOne();

        assertNotNull(record);

        assertEquals(category.getId(), record.getId());
        assertEquals(category.getUserId(), record.getUserId());
        assertEquals(
            category.getName(),
            record.getName()
        );
        assertEquals(category.getColor(), record.getColor());
        assertEquals(category.getIcon(), record.getIcon());
        assertNull(record.getDeletedAt());

        assertInstantEquals(
            category.getCreatedAt(),
            record.getCreatedAt().toInstant()
        );

        assertInstantEquals(
            category.getUpdatedAt(),
            record.getUpdatedAt().toInstant()
        );

        assertEquals(category.getVersion(), record.getVersion());
    }

    @Test
    @Transactional
    void shouldFindCategoryById() {
        Category category = createCategoryForTest();

        Optional<Category> result =
            categoryRepository.findById(category.getId());

        assertTrue(result.isPresent());

        Category found = result.orElseThrow();

        assertEquals(category.getId(), found.getId());
        assertEquals(category.getUserId(), found.getUserId());
        assertEquals(category.getName(), found.getName());
        assertEquals(category.getColor(), found.getColor());
        assertEquals(category.getIcon(), found.getIcon());
        assertNull(found.getDeletedAt());
        assertEquals(category.getVersion(), found.getVersion());
    }

    @Test
    @Transactional
    void shouldReturnEmptyWhenCategoryDoesNotExist() {
        Optional<Category> result =
            categoryRepository.findById(UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    void shouldFindCategoryByIdAndUserId() {
        Category category = createCategoryForTest();

        Optional<Category> result =
            categoryRepository.findByIdAndUserId(
                category.getId(),
                category.getUserId()
            );

        assertTrue(result.isPresent());

        Category found = result.orElseThrow();

        assertEquals(category.getId(), found.getId());
        assertEquals(category.getUserId(), found.getUserId());
        assertEquals(category.getName(), found.getName());
    }

    @Test
    @Transactional
    void shouldNotFindCategoryWhenUserDoesNotOwnIt() {
        Category category = createCategoryForTest();

        UUID anotherUserId = createUserForTest();

        Optional<Category> result =
            categoryRepository.findByIdAndUserId(
                category.getId(),
                anotherUserId
            );

        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    void shouldNotFindDeletedCategory() {
        UUID userId = createUserForTest();

        Category category = Category.restore(
            UUID.randomUUID(),
            userId,
            "Food",
            "#FF0000",
            "restaurant",
            Instant.now(),
            Instant.now(),
            Instant.now(),
            1
        );

        categoryRepository.save(category);

        Optional<Category> result =
            categoryRepository.findByIdAndUserId(
                category.getId(),
                userId
            );

        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    void shouldSoftDeleteCategory() {
        Category category = createCategoryForTest();

        String originalName = category.getName();

        category.delete();

        categoryRepository.update(category);

        CategoryRecord record = dsl.selectFrom(CATEGORY)
            .where(CATEGORY.ID.eq(category.getId()))
            .fetchOne();

        assertNotNull(record);

        assertEquals(category.getId(), record.getId());
        assertEquals(category.getUserId(), record.getUserId());

        assertEquals(
            originalName,
            record.getName()
        );

        assertNotNull(record.getDeletedAt());

        assertInstantEquals(
            category.getDeletedAt(),
            record.getDeletedAt().toInstant()
        );

        assertEquals(
            category.getVersion(),
            record.getVersion()
        );
    }

    @Test
    @Transactional
    void shouldKeepDeletedCategoryInDatabase() {
        Category category = createCategoryForTest();

        category.delete();

        categoryRepository.update(category);

        CategoryRecord record = dsl.selectFrom(CATEGORY)
            .where(CATEGORY.ID.eq(category.getId()))
            .fetchOne();

        assertNotNull(record);
        assertNotNull(record.getDeletedAt());
    }

    @Test
    @Transactional
    void shouldUpdateCategory() {
        Category category = createCategoryForTest();

        category.update(
            "Restaurants",
            "#00FF00",
            "dining"
        );

        categoryRepository.update(category);

        CategoryRecord record = dsl.selectFrom(CATEGORY)
            .where(CATEGORY.ID.eq(category.getId()))
            .fetchOne();

        assertNotNull(record);

        assertEquals(
            "Restaurants",
            record.getName()
        );
        assertEquals("#00FF00", record.getColor());
        assertEquals("dining", record.getIcon());
        assertEquals(
            category.getVersion(),
            record.getVersion()
        );

        assertInstantEquals(
            category.getUpdatedAt(),
            record.getUpdatedAt().toInstant()
        );
    }

    @Test
    @Transactional
    void shouldFindCategoriesByUserId() {
        UUID userId = createUserForTest();

        categoryRepository.save(
            Category.create(
                userId,
                "Food",
                "#FF0000",
                "restaurant"
            )
        );

        categoryRepository.save(
            Category.create(
                userId,
                "Transport",
                "#0000FF",
                "directions_car"
            )
        );

        PageResult<Category> result =
            categoryRepository.findByUserId(
                userId,
                new PageRequest(0, 10)
            );

        assertEquals(2, result.content().size());
        assertEquals(2, result.totalElements());
        assertEquals(0, result.page());
        assertEquals(10, result.size());

        assertEquals(
            "Food",
            result.content().getFirst().getName()
        );

        assertEquals(
            "Transport",
            result.content().get(1).getName()
        );
    }
    @Test
    @Transactional
    void shouldNotReturnDeletedCategories() {
        UUID userId = createUserForTest();

        Category activeCategory = categoryRepository.save(
            Category.create(
                userId,
                "Food",
                "#FF0000",
                "restaurant"
            )
        );

        Category deletedCategory = categoryRepository.save(
            Category.create(
                userId,
                "Transport",
                "#0000FF",
                "directions_car"
            )
        );

        deletedCategory.delete();

        categoryRepository.update(deletedCategory);

        PageResult<Category> result =
            categoryRepository.findByUserId(
                userId,
                new PageRequest(0, 10)
            );

        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements());

        assertEquals(
            activeCategory.getId(),
            result.content().getFirst().getId()
        );
    }

    @Test
    @Transactional
    void shouldPaginateCategories() {
        UUID userId = createUserForTest();

        categoryRepository.save(
            Category.create(
                userId,
                "Food",
                "#FF0000",
                "restaurant"
            )
        );

        categoryRepository.save(
            Category.create(
                userId,
                "Shopping",
                "#00FF00",
                "shopping_cart"
            )
        );

        categoryRepository.save(
            Category.create(
                userId,
                "Transport",
                "#0000FF",
                "directions_car"
            )
        );

        PageResult<Category> result =
            categoryRepository.findByUserId(
                userId,
                new PageRequest(1, 2)
            );

        assertEquals(1, result.content().size());
        assertEquals(3, result.totalElements());
        assertEquals(1, result.page());
        assertEquals(2, result.size());

        assertEquals(
            "Transport",
            result.content().getFirst().getName()
        );
    }


}
