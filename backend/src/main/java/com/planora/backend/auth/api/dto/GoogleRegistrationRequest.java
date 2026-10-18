package com.planora.backend.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GoogleRegistrationRequest(
    @NotBlank
    @Pattern(regexp = "[a-zA-Z0-9._-]{3,40}")
    String username,

    @NotBlank
    @Size(max = 100)
    String firstName,

    @NotBlank
    @Size(max = 100)
    String lastName,

    @NotNull
    Short primaryCurrencyId
) {
}
