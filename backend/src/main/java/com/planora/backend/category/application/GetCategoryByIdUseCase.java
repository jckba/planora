package com.planora.backend.category.application;

import com.planora.backend.category.domain.Category;

import java.util.UUID;

public interface GetCategoryByIdUseCase {

    Category execute(UUID userId, UUID categoryId);
}
