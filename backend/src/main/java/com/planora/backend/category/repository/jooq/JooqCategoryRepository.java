package com.planora.backend.category.repository.jooq;

import com.planora.backend.category.domain.Category;
import com.planora.backend.category.repository.CategoryRepository;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.persistence.jooq.tables.records.CategoryRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.planora.persistence.jooq.tables.Category.CATEGORY;

@Repository
public class JooqCategoryRepository implements CategoryRepository {

    private final DSLContext dsl;

    public JooqCategoryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Category save(Category category) {
        dsl.insertInto(CATEGORY)
            .set(CATEGORY.ID, category.getId())
            .set(CATEGORY.USER_ID, category.getUserId())
            .set(CATEGORY.NAME, category.getName())
            .set(CATEGORY.COLOR, category.getColor())
            .set(CATEGORY.ICON, category.getIcon())
            .set(
                CATEGORY.DELETED_AT,
                category.getDeletedAt() == null
                    ? null
                    : OffsetDateTime.ofInstant(
                    category.getDeletedAt(),
                    ZoneOffset.UTC
                )
            )
            .set(
                CATEGORY.CREATED_AT,
                OffsetDateTime.ofInstant(
                    category.getCreatedAt(),
                    ZoneOffset.UTC
                )
            )
            .set(
                CATEGORY.UPDATED_AT,
                OffsetDateTime.ofInstant(
                    category.getUpdatedAt(),
                    ZoneOffset.UTC
                )
            )
            .set(CATEGORY.VERSION, category.getVersion())
            .execute();

        return category;
    }

    @Override
    public void update(Category category) {
        dsl.update(CATEGORY)
            .set(CATEGORY.NAME, category.getName())
            .set(CATEGORY.COLOR, category.getColor())
            .set(CATEGORY.ICON, category.getIcon())
            .set(CATEGORY.DELETED_AT, category.getDeletedAt() == null
                    ? null
                    : OffsetDateTime.ofInstant(
                    category.getDeletedAt(),
                    ZoneOffset.UTC
                )
            )
            .set(CATEGORY.UPDATED_AT, category.getUpdatedAt() == null
                    ? null
                    : OffsetDateTime.ofInstant(
                    category.getUpdatedAt(),
                    ZoneOffset.UTC
                )
            )
            .set(CATEGORY.VERSION, category.getVersion())
            .where(CATEGORY.ID.eq(category.getId()))
            .execute();
    }

    @Override
    public Optional<Category> findById(UUID id) {
        CategoryRecord record = dsl.selectFrom(CATEGORY)
            .where(CATEGORY.ID.eq(id)
                .and(CATEGORY.DELETED_AT.isNull())
            )
            .fetchOne();
        return Optional.ofNullable(record)
            .map(this::mapToCategory);
    }

    @Override
    public Optional<Category> findByIdAndUserId(UUID id, UUID userId) {
        CategoryRecord record = dsl.selectFrom(CATEGORY)
            .where(
                CATEGORY.ID.eq(id)
                    .and(CATEGORY.USER_ID.eq(userId))
                    .and(CATEGORY.DELETED_AT.isNull())
            )
            .fetchOne();

        return Optional.ofNullable(record)
            .map(this::mapToCategory);
    }

    @Override
    public PageResult<Category> findByUserId(UUID userId, PageRequest pageRequest) {
        int offset = pageRequest.page() * pageRequest.size();

        List<CategoryRecord> records = dsl.selectFrom(CATEGORY)
            .where(CATEGORY.USER_ID.eq(userId).and(CATEGORY.DELETED_AT.isNull()))
            .orderBy(CATEGORY.NAME.asc(), CATEGORY.ID.asc())
            .limit(pageRequest.size())
            .offset(offset)
            .fetch();

        List<Category> categories = records.stream()
            .map(this::mapToCategory)
            .toList();

        long totalElements = Optional.ofNullable(
            dsl.selectCount()
            .from(CATEGORY)
            .where(
                CATEGORY.USER_ID.eq(userId)
                    .and(CATEGORY.DELETED_AT.isNull())
            )
            .fetchOne(0, Long.class))
            .orElse(0L);

        return new PageResult<>(
            categories,
            pageRequest.page(),
            pageRequest.size(),
            totalElements
        );
    }

    private Category mapToCategory(CategoryRecord record) {
        return Category.restore(
            record.getId(),
            record.getUserId(),
            record.getName(),
            record.getColor(),
            record.getIcon(),
            record.getDeletedAt() == null
                ? null
                : record.getDeletedAt().toInstant(),
            record.getCreatedAt().toInstant(),
            record.getUpdatedAt().toInstant(),
            record.getVersion()
        );
    }
}
