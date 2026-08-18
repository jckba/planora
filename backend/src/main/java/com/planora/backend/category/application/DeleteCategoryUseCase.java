package com.planora.backend.category.application;

import java.util.UUID;

public interface DeleteCategoryUseCase {
    void execute(UUID userId, UUID categoryId);
}
