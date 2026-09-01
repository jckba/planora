package com.planora.backend.category.application.create;

import com.planora.backend.category.domain.Category;
import com.planora.backend.category.repository.CategoryRepository;
import com.planora.backend.common.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateCategoryService implements CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CurrentUser currentUser;

    public CreateCategoryService(CategoryRepository categoryRepository, CurrentUser currentUser) {
        this.categoryRepository = categoryRepository;
        this.currentUser = currentUser;
    }

    @Override
    public Category execute(CreateCategoryCommand command) {
        UUID userId = currentUser.userId();

        Category category = Category.create(
            userId,
            command.name(),
            command.color(),
            command.icon()
        );
        return categoryRepository.save(category);
    }
}
