package com.planora.backend.common.pagination;

public record PageRequest(
    int page,
    int size
) {
    public PageRequest {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }
    }
}
