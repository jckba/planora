package com.planora.backend.category.api;

import com.planora.backend.category.api.dto.CategoryResponse;
import com.planora.backend.category.api.dto.CreateCategoryRequest;
import com.planora.backend.category.api.dto.UpdateCategoryRequest;
import com.planora.backend.category.application.create.CreateCategoryCommand;
import com.planora.backend.category.application.create.CreateCategoryUseCase;
import com.planora.backend.category.application.delete.DeleteCategoryUseCase;
import com.planora.backend.category.application.get.GetCategoriesUseCase;
import com.planora.backend.category.application.get.GetCategoryByIdUseCase;
import com.planora.backend.category.application.update.UpdateCategoryCommand;
import com.planora.backend.category.application.update.UpdateCategoryUseCase;
import com.planora.backend.category.domain.Category;
import com.planora.backend.common.api.PageResponse;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final GetCategoriesUseCase getCategoriesUseCase;
    private final GetCategoryByIdUseCase getCategoryByIdUseCase;
    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;

    public CategoryController(GetCategoriesUseCase getCategoriesUseCase, GetCategoryByIdUseCase getCategoryByIdUseCase, CreateCategoryUseCase createCategoryUseCase, UpdateCategoryUseCase updateCategoryUseCase, DeleteCategoryUseCase deleteCategoryUseCase) {
        this.getCategoriesUseCase = getCategoriesUseCase;
        this.getCategoryByIdUseCase = getCategoryByIdUseCase;
        this.createCategoryUseCase = createCategoryUseCase;
        this.updateCategoryUseCase = updateCategoryUseCase;
        this.deleteCategoryUseCase = deleteCategoryUseCase;
    }

    @GetMapping
    public PageResponse<CategoryResponse> getCategories(
        @RequestParam UUID userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageRequest = new PageRequest(page, size);
        PageResult<Category> result = getCategoriesUseCase.execute(userId, pageRequest);
        List<CategoryResponse> content = result.content().stream().map(this::toResponse).toList();

        return new PageResponse<>(content,
            result.page(),
            result.size(),
            result.totalElements()
        );
    }

    @GetMapping("/{categoryId}")
    public CategoryResponse getCategoryById(
        @RequestParam UUID userId,
        @PathVariable UUID categoryId
    ) {
        Category category = getCategoryByIdUseCase.execute(userId, categoryId);
        return toResponse(category);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(
        @Valid @RequestBody CreateCategoryRequest request
    ) {
        CreateCategoryCommand command = new CreateCategoryCommand(
            request.name(),
            request.color(),
            request.icon()
        );

        Category category = createCategoryUseCase.execute(command);

        return toResponse(category);
    }

    @PutMapping("/{categoryId}")
    public CategoryResponse updateCategory(
        @RequestParam UUID userId,
        @PathVariable UUID categoryId,
        @Valid @RequestBody UpdateCategoryRequest request
    ) {
        UpdateCategoryCommand command = new UpdateCategoryCommand(request.name(), request.color(), request.icon());

        Category category = updateCategoryUseCase.execute(userId, categoryId, command);

        return toResponse(category);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(
        @RequestParam UUID userId,
        @PathVariable UUID categoryId
    ) {
        deleteCategoryUseCase.execute(userId, categoryId);
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getColor(),
            category.getIcon()
        );
    }
}
