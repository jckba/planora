package com.planora.backend.category.api.dto;

import java.util.UUID;

public record CategoryResponse (
    UUID id,
    String name,
    String color,
    String icon
) {
}
