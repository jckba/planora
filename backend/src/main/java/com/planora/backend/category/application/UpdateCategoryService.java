package com.planora.backend.category.application;

import com.planora.backend.category.domain.Category;
import com.planora.backend.category.repository.CategoryRepository;
import com.planora.backend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateCategoryService implements UpdateCategoryUseCase{

    private final CategoryRepository categoryRepository;

    public UpdateCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category execute(UUID userId, UUID categoryId, UpdateCategoryCommand command) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        category.update(
            command.name(),
            command.color(),
            command.icon()
        );
        categoryRepository.update(category);
        return category;
    }
}
