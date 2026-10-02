package com.planora.backend.category.application.update;

import com.planora.backend.category.domain.Category;
import com.planora.backend.category.repository.CategoryRepository;
import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.common.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateCategoryService implements UpdateCategoryUseCase{

    private final CategoryRepository categoryRepository;
    private final CurrentUser currentUser;

    public UpdateCategoryService(CategoryRepository categoryRepository, CurrentUser currentUser) {
        this.categoryRepository = categoryRepository;
        this.currentUser = currentUser;
    }

    @Override
    public Category execute(UUID categoryId, UpdateCategoryCommand command) {
        UUID userId = currentUser.userId();
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
