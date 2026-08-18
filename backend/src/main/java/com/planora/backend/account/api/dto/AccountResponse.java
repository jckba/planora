package com.planora.backend.account.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
    UUID id,
    Short accountTypeId,
    Short currencyId,
    String name,
    BigDecimal balance
) {
}
