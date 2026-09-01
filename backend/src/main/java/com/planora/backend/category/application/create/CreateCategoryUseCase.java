package com.planora.backend.category.application.create;

import com.planora.backend.category.domain.Category;

public interface CreateCategoryUseCase {
    Category execute(CreateCategoryCommand command);
}
