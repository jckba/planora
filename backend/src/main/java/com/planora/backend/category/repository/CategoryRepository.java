package com.planora.backend.category.repository;

import com.planora.backend.category.domain.Category;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {

    Category save(Category category);

    void update(Category category);

    Optional<Category> findById(UUID id);

    Optional<Category> findByIdAndUserId(UUID id, UUID userId);

    PageResult<Category> findByUserId(UUID userId, PageRequest pageRequest);
}
