package com.planora.backend.category.application;

import com.planora.backend.category.application.delete.DeleteCategoryService;
import com.planora.backend.category.domain.Category;
import com.planora.backend.category.repository.CategoryRepository;
import com.planora.backend.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private DeleteCategoryService deleteCategoryService;

    @Test
    void shouldDeleteCategory() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Category category = Category.create(
            userId,
            "Food",
            "#FF0000",
            "restaurant"
        );

        when(
            categoryRepository.findByIdAndUserId(
                categoryId,
                userId
            )
        ).thenReturn(Optional.of(category));

        deleteCategoryService.execute(userId, categoryId);

        ArgumentCaptor<Category> captor =
            ArgumentCaptor.forClass(Category.class);

        verify(categoryRepository).update(captor.capture());

        Category updatedCategory = captor.getValue();

        assertNotNull(updatedCategory.getDeletedAt());
        assertEquals(1, updatedCategory.getVersion());
    }

    @Test
    void shouldThrowWhenCategoryDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(
            categoryRepository.findByIdAndUserId(
                categoryId,
                userId
            )
        ).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> deleteCategoryService.execute(
                userId,
                categoryId
            )
        );

        verify(categoryRepository, never()).update(any(Category.class));
    }

}
