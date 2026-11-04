package com.planora.backend.auth.application.user.register;

public record CompleteProfileCommand(
    String username,
    short primaryCurrencyId
) {
}
