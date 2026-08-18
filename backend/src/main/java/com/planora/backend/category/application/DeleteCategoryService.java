package com.planora.backend.category.application;

import com.planora.backend.category.domain.Category;
import com.planora.backend.category.repository.CategoryRepository;
import com.planora.backend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteCategoryService implements DeleteCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public DeleteCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void execute(UUID userId, UUID categoryId) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.delete();
        categoryRepository.update(category);
    }
}
