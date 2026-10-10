package com.planora.backend.account.application.update;

import java.util.UUID;

public record UpdateAccountCommand(
    UUID accountId,
    Short accountTypeId,
    Short currencyId,
    String name
) {
}
