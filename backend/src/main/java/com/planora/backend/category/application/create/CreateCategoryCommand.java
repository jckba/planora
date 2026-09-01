package com.planora.backend.category.application.create;

public record CreateCategoryCommand(
    String name,
    String color,
    String icon
) {
}
