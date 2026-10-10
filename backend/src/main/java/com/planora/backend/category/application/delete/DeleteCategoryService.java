package com.planora.backend.category.application.delete;

import com.planora.backend.category.domain.Category;
import com.planora.backend.category.repository.CategoryRepository;
import com.planora.backend.common.exception.CategoryNotFoundException;
import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.common.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteCategoryService implements DeleteCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CurrentUser currentUser;

    public DeleteCategoryService(CategoryRepository categoryRepository, CurrentUser currentUser) {
        this.categoryRepository = categoryRepository;
        this.currentUser = currentUser;
    }

    @Override
    public void execute(UUID categoryId) {
        UUID userId = currentUser.userId();
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
            .orElseThrow(CategoryNotFoundException::new);
        category.delete();
        categoryRepository.update(category);
    }
}
