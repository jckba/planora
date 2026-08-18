package com.planora.backend.category.application;

import com.planora.backend.category.domain.Category;

import java.util.UUID;

public interface CreateCategoryUseCase {

    Category execute(UUID userId, CreateCategoryCommand command);
}
