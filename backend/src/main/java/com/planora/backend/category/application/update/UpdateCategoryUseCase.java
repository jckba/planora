package com.planora.backend.category.application.update;

import com.planora.backend.category.domain.Category;

import java.util.UUID;

public interface UpdateCategoryUseCase {
    Category execute(
        UUID categoryId,
        UpdateCategoryCommand command
    );
}
