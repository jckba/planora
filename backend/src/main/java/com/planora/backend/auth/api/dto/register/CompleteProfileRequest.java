package com.planora.backend.auth.api.dto.register;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CompleteProfileRequest(
    @NotBlank
    @Size(min = 3, max = 20)
    String username,

    @Positive
    short primaryCurrencyId,

    @Size(max = 100)
    String firstName,

    @Size(max = 100)
    String lastName
) {
}
