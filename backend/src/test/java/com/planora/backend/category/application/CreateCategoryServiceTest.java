package com.planora.backend.category.application;

import com.planora.backend.category.domain.Category;
import com.planora.backend.category.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CreateCategoryService createCategoryService;

    @Test
    void shouldCreateCategory() {
        UUID userId = UUID.randomUUID();

        CreateCategoryCommand command = new CreateCategoryCommand(
            "Food",
            "#FF0000",
            "restaurant"
        );

        Category savedCategory = Category.create(
            userId,
            command.name(),
            command.color(),
            command.icon()
        );

        when(categoryRepository.save(any(Category.class)))
            .thenReturn(savedCategory);

        Category result =
            createCategoryService.execute(userId, command);

        assertSame(savedCategory, result);

        ArgumentCaptor<Category> captor =
            ArgumentCaptor.forClass(Category.class);

        verify(categoryRepository).save(captor.capture());

        Category createdCategory = captor.getValue();

        assertEquals(userId, createdCategory.getUserId());
        assertEquals(command.name(), createdCategory.getName());
        assertEquals(command.color(), createdCategory.getColor());
        assertEquals(command.icon(), createdCategory.getIcon());

        assertNotNull(createdCategory.getId());
        assertNotNull(createdCategory.getCreatedAt());
        assertNotNull(createdCategory.getUpdatedAt());

        assertEquals(0, createdCategory.getVersion());
    }

    @Test
    void shouldRejectInvalidCategory() {
        UUID userId = UUID.randomUUID();

        CreateCategoryCommand command = new CreateCategoryCommand(
            "   ",
            "#FF0000",
            "restaurant"
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> createCategoryService.execute(userId, command)
        );

        verifyNoInteractions(categoryRepository);
    }

}
