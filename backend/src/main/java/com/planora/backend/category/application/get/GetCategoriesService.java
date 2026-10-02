package com.planora.backend.category.application.get;

import com.planora.backend.category.domain.Category;
import com.planora.backend.category.repository.CategoryRepository;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.common.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetCategoriesService implements GetCategoriesUseCase{

    private final CategoryRepository categoryRepository;
    private final CurrentUser currentUser;

    public GetCategoriesService(CategoryRepository categoryRepository, CurrentUser currentUser) {
        this.categoryRepository = categoryRepository;
        this.currentUser = currentUser;
    }

    @Override
    public PageResult<Category> execute(PageRequest pageRequest) {
        UUID userId = currentUser.userId();
        return categoryRepository.findByUserId(userId, pageRequest);
    }
}
