package com.planora.backend.category.application.get;

import com.planora.backend.category.domain.Category;
import com.planora.backend.category.repository.CategoryRepository;
import com.planora.backend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetCategoryByIdService implements GetCategoryByIdUseCase{

    private final CategoryRepository categoryRepository;

    public GetCategoryByIdService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category execute(UUID userId, UUID categoryId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Category not found")
            );
    }
}
