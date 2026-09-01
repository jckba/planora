package com.planora.backend.category.application;

import com.planora.backend.category.application.get.GetCategoryByIdService;
import com.planora.backend.category.domain.Category;
import com.planora.backend.category.repository.CategoryRepository;
import com.planora.backend.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetCategoryByIdServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private GetCategoryByIdService getCategoryByIdService;

    @Test
    void shouldFindCategoryById() {
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

        Category result =
            getCategoryByIdService.execute(userId, categoryId);

        assertSame(category, result);

        verify(categoryRepository)
            .findByIdAndUserId(categoryId, userId);
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
            () -> getCategoryByIdService.execute(
                userId,
                categoryId
            )
        );

        verify(categoryRepository)
            .findByIdAndUserId(categoryId, userId);
    }

    @Test
    void shouldThrowWhenCategoryDoesNotBelongToUser() {
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
            () -> getCategoryByIdService.execute(
                userId,
                categoryId
            )
        );

        verify(categoryRepository)
            .findByIdAndUserId(categoryId, userId);
    }

}
