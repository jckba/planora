package com.planora.backend.auth.application.user.register;

public record CompleteLocalUserCommand(
    String username,
    short primaryCurrencyId
) {
}
