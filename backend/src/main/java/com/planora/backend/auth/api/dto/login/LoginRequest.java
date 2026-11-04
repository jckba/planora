package com.planora.backend.auth.api.dto.login;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

    @NotBlank
    String identifier,
    @NotBlank
    String password
) {
}
