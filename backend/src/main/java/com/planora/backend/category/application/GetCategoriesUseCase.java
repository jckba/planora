package com.planora.backend.category.application;

import com.planora.backend.category.domain.Category;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;

import java.util.UUID;

public interface GetCategoriesUseCase {

    PageResult<Category> execute(UUID userId, PageRequest pageRequest);
}
