package com.planora.backend.category.application.get;

import com.planora.backend.category.domain.Category;
import com.planora.backend.category.repository.CategoryRepository;
import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.common.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetCategoryByIdService implements GetCategoryByIdUseCase{

    private final CategoryRepository categoryRepository;
    private final CurrentUser currentUser;

    public GetCategoryByIdService(CategoryRepository categoryRepository, CurrentUser currentUser) {
        this.categoryRepository = categoryRepository;
        this.currentUser = currentUser;
    }

    @Override
    public Category execute(UUID categoryId) {
        UUID userId = currentUser.userId();
        return categoryRepository.findByIdAndUserId(categoryId, userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Category not found")
            );
    }
}
