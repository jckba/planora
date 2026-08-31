package com.planora.backend.common.pagination;

public record PageRequest(
    int page,
    int size
) {

    private static final int MAX_PAGE_SIZE = 100;

    public PageRequest {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }

        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size cannot be greater than " + MAX_PAGE_SIZE);
        }
    }
}
