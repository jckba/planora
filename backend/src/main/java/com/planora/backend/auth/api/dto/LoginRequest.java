package com.planora.backend.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

    @NotBlank
    String identifier,
    @NotBlank
    String password
) {
}
