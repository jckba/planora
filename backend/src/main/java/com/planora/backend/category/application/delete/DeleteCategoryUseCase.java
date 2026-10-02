package com.planora.backend.category.application.delete;

import java.util.UUID;

public interface DeleteCategoryUseCase {
    void execute(UUID categoryId);
}
