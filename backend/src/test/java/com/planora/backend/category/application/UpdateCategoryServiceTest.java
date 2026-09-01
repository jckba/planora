package com.planora.backend.category.application;

import com.planora.backend.category.application.update.UpdateCategoryCommand;
import com.planora.backend.category.application.update.UpdateCategoryService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private UpdateCategoryService updateCategoryService;

    @Test
    void shouldUpdateCategory() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Category category = Category.create(
            userId,
            "Food",
            "#FF0000",
            "restaurant"
        );

        UpdateCategoryCommand command = new UpdateCategoryCommand(
            "Restaurants",
            "#00FF00",
            "dining"
        );

        when(
            categoryRepository.findByIdAndUserId(categoryId, userId)
        ).thenReturn(Optional.of(category));
        Category result = updateCategoryService.execute(
            userId,
            categoryId,
            command
        );

        assertSame(category, result);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);

        verify(categoryRepository).update(captor.capture());

        Category updatedCategory = captor.getValue();

        assertEquals("Restaurants", updatedCategory.getName());
        assertEquals("#00FF00", updatedCategory.getColor());
        assertEquals("dining", updatedCategory.getIcon());
        assertEquals(1, updatedCategory.getVersion());
    }

    @Test
    void shouldThrowWhenCategoryDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        UpdateCategoryCommand command =
            new UpdateCategoryCommand(
                "Restaurants",
                "#00FF00",
                "dining"
            );

        when(
            categoryRepository.findByIdAndUserId(
                categoryId,
                userId
            )
        ).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> updateCategoryService.execute(
                userId,
                categoryId,
                command
            )
        );

        verify(categoryRepository, never())
            .update(any(Category.class));
    }
}


