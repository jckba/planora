package com.planora.backend.category.application.get;

import com.planora.backend.category.domain.Category;
import com.planora.backend.category.repository.CategoryRepository;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetCategoriesService implements GetCategoriesUseCase{

    private final CategoryRepository categoryRepository;

    public GetCategoriesService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public PageResult<Category> execute(UUID userId, PageRequest pageRequest) {
        return categoryRepository.findByUserId(userId, pageRequest);
    }
}
