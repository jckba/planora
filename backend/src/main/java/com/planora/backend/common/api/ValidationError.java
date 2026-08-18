package com.planora.backend.common.api;

public record ValidationError(
    String field,
    String message
) {
}
