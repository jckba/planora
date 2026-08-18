package com.planora.backend.common.api;

import java.util.List;

public record ErrorResponse(
    String code,
    String message,
    List<ValidationError> errors
) {
}
