package com.planora.backend.category.application.update;

public record UpdateCategoryCommand(
    String name,
    String color,
    String icon
) {
}
