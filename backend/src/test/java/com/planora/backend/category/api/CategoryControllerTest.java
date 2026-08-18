package com.planora.backend.category.api;

import com.planora.backend.category.api.dto.CreateCategoryRequest;
import com.planora.backend.category.application.*;
import com.planora.backend.category.domain.Category;
import com.planora.backend.common.api.GlobalExceptionHandler;
import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import(GlobalExceptionHandler.class)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetCategoriesUseCase getCategoriesUseCase;

    @MockitoBean
    private GetCategoryByIdUseCase getCategoryByIdUseCase;

    @MockitoBean
    private CreateCategoryUseCase createCategoryUseCase;

    @MockitoBean
    private UpdateCategoryUseCase updateCategoryUseCase;

    @MockitoBean
    private DeleteCategoryUseCase deleteCategoryUseCase;

    @Test
    void shouldGetCategories() throws Exception {
        UUID userId = UUID.randomUUID();

        Category category = Category.create(
            userId,
            "Food",
            "#FF0000",
            "restaurant"
        );

        PageRequest pageRequest = new PageRequest(0, 10);
        PageResult<Category> pageResult = new PageResult<>(List.of(category), 0, 10, 1);
        when(getCategoriesUseCase.execute(userId, pageRequest)).thenReturn(pageResult);

        mockMvc.perform(
                get("/api/categories")
                    .param("userId", userId.toString())
                    .param("page", "0")
                    .param("size", "10")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id")
                .value(category.getId().toString()))
            .andExpect(jsonPath("$.content[0].name")
                .value("Food"))
            .andExpect(jsonPath("$.content[0].color")
                .value("#FF0000"))
            .andExpect(jsonPath("$.content[0].icon")
                .value("restaurant"))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(1));

        verify(getCategoriesUseCase)
            .execute(userId, pageRequest);
    }

    @Test
    void shouldGetCategoryById() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Category category = Category.create(
            userId,
            "Food",
            "#FF0000",
            "restaurant"
        );

        when(
            getCategoryByIdUseCase.execute(
                userId,
                categoryId
            )
        ).thenReturn(category);

        mockMvc.perform(
                get("/api/categories/{categoryId}", categoryId)
                    .param("userId", userId.toString())
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.id")
                    .value(category.getId().toString())
            )
            .andExpect(
                jsonPath("$.name")
                    .value("Food")
            )
            .andExpect(
                jsonPath("$.color")
                    .value("#FF0000")
            )
            .andExpect(
                jsonPath("$.icon")
                    .value("restaurant")
            );

        verify(getCategoryByIdUseCase)
            .execute(userId, categoryId);
    }

    @Test
    void shouldReturn404WhenCategoryDoesNotExist() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(
            getCategoryByIdUseCase.execute(
                userId,
                categoryId
            )
        ).thenThrow(
            new ResourceNotFoundException("Category not found")
        );
        mockMvc.perform(
                get("/api/categories/{categoryId}", categoryId)
                    .param("userId", userId.toString())
            )
            .andExpect(status().isNotFound())
            .andExpect(
                jsonPath("$.code")
                    .value("RESOURCE_NOT_FOUND")
            )
            .andExpect(
                jsonPath("$.message")
                    .value("Category not found")
            );

        verify(getCategoryByIdUseCase)
            .execute(userId, categoryId);
    }

    @Test
    void shouldCreateCategory() throws Exception {
        UUID userId = UUID.randomUUID();
        Category category = Category.create(
            userId,
            "Food",
            "#FF0000",
            "restaurant"
        );

        CreateCategoryRequest request =
            new CreateCategoryRequest(
                "Food",
                "#FF0000",
                "restaurant"
            );

        CreateCategoryCommand command =
            new CreateCategoryCommand(
                request.name(),
                request.color(),
                request.icon()
            );

        when(
            createCategoryUseCase.execute(
                eq(userId),
                eq(command)
            )
        ).thenReturn(category);

        mockMvc.perform(
                post("/api/categories")
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                {
                    "name": "Food",
                    "color": "#FF0000",
                    "icon": "restaurant"
                }
                """)
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.id")
                    .value(category.getId().toString())
            )
            .andExpect(
                jsonPath("$.name")
                    .value("Food")
            )
            .andExpect(
                jsonPath("$.color")
                    .value("#FF0000")
            )
            .andExpect(
                jsonPath("$.icon")
                    .value("restaurant")
            );

        verify(createCategoryUseCase)
            .execute(userId, command);
    }

    @Test
    void shouldRejectCreateCategoryWithoutBody() throws Exception {
        UUID userId = UUID.randomUUID();
        mockMvc.perform(
            post("/api/categories")
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
        verifyNoInteractions(createCategoryUseCase);
    }

    @Test
    void shouldRejectCreateCategoryWithBlankName() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(
                post("/api/categories")
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                {
                    "name": "",
                    "color": "#FF0000",
                    "icon": "restaurant"
                }
                """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.code")
                    .value("VALIDATION_ERROR")
            )
            .andExpect(
                jsonPath("$.message")
                    .value("Request validation failed")
            )
            .andExpect(
                jsonPath("$.errors[0].field")
                    .value("name")
            );

        verifyNoInteractions(createCategoryUseCase);
    }

    @Test
    void shouldRejectUpdateCategoryWithBlankName() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        mockMvc.perform(
                put("/api/categories/{categoryId}", categoryId)
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                {
                    "name": "",
                    "color": "#00FF00",
                    "icon": "dining"
                }
                """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.code")
                    .value("VALIDATION_ERROR")
            )
            .andExpect(
                jsonPath("$.errors[0].field")
                    .value("name")
            );

        verifyNoInteractions(updateCategoryUseCase);
    }

    @Test
    void shouldUpdateCategory() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Category category = Category.create(
            userId,
            "Restaurants",
            "#00FF00",
            "dining"
        );

        UpdateCategoryCommand command =
            new UpdateCategoryCommand(
                "Restaurants",
                "#00FF00",
                "dining"
            );

        when(
            updateCategoryUseCase.execute(
                eq(userId),
                eq(categoryId),
                eq(command)
            )
        ).thenReturn(category);

        mockMvc.perform(
                put("/api/categories/{categoryId}", categoryId)
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                {
                    "name": "Restaurants",
                    "color": "#00FF00",
                    "icon": "dining"
                }
                """)
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.name")
                    .value("Restaurants")
            )
            .andExpect(
                jsonPath("$.color")
                    .value("#00FF00")
            )
            .andExpect(
                jsonPath("$.icon")
                    .value("dining")
            );

        verify(updateCategoryUseCase)
            .execute(userId, categoryId, command);
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        mockMvc.perform(delete("/api/categories/{categoryId}", categoryId)
            .param("userId", userId.toString())
            )
            .andExpect(status().isNoContent());

        verify(deleteCategoryUseCase)
            .execute(userId, categoryId);
    }





}
