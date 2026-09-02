package com.planora.backend.auth.application.user;

public record CreateUserCommand(
    String username,
    String email,
    String password,
    String firstName,
    String lastName,
    Short primaryCurrencyId
) {
}
