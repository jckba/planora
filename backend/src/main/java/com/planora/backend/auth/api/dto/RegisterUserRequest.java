package com.planora.backend.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(

    @NotBlank
    @Size(min = 3, max = 50)
    String username,

    @NotBlank
    @Email
    @Size(max = 254)
    String email,

    @NotBlank
    @Size(min = 8, max = 27)
    String password,

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
