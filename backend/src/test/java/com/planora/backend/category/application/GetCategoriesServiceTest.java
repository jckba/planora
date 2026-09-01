package com.planora.backend.category.application;

import com.planora.backend.category.application.get.GetCategoriesService;
import com.planora.backend.category.domain.Category;
import com.planora.backend.category.repository.CategoryRepository;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetCategoriesServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private GetCategoriesService getCategoriesService;

    @Test
    void shouldGetCategories() {
        UUID userId = UUID.randomUUID();
        PageRequest pageRequest = new PageRequest(0, 10);

        PageResult<Category> expected = new PageResult<>(List.of(
            Category.create(
                userId,
                "Food",
                "#FF0000",
                "restaurant"
            )
        ),
            0, 10, 1
        );

        when(
            categoryRepository.findByUserId(
                userId,
                pageRequest
            )
        ).thenReturn(expected);

        PageResult<Category> result =
            getCategoriesService.execute(
                userId,
                pageRequest
            );

        assertSame(expected, result);

        verify(categoryRepository)
            .findByUserId(userId, pageRequest);
    }
}
