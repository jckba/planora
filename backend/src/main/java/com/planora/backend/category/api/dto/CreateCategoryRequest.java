package com.planora.backend.category.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
    @NotBlank
    String name,

    @NotBlank
    String color,

    @NotBlank
    String icon
) {
}
