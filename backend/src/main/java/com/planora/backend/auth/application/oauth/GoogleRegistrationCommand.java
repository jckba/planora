package com.planora.backend.auth.application.oauth;

public record GoogleRegistrationCommand(
    String username,
    String firstName,
    String lastName,
    short primaryCurrencyId
) {
}
