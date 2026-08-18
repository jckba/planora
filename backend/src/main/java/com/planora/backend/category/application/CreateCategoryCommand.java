package com.planora.backend.category.application;

public record CreateCategoryCommand(
    String name,
    String color,
    String icon
) {
}
