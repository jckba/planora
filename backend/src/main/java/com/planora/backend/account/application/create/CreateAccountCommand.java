package com.planora.backend.account.application.create;

import java.util.UUID;

public record CreateAccountCommand(
    UUID userId,
    Short accountTypeId,
    Short currencyId,
    String name
) {
}
