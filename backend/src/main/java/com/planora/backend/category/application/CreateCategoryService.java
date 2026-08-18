package com.planora.backend.category.application;

import com.planora.backend.category.domain.Category;
import com.planora.backend.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateCategoryService implements CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public CreateCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category execute(UUID userId, CreateCategoryCommand command) {
        Category category = Category.create(
            userId,
            command.name(),
            command.color(),
            command.icon()
        );
        return categoryRepository.save(category);
    }
}
