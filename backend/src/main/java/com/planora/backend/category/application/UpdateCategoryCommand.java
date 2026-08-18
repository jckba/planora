package com.planora.backend.category.application;

public record UpdateCategoryCommand(
    String name,
    String color,
    String icon
) {
}
