package com.planora.backend.account.api.dto;

public record CreateAccountRequest(
    Short accountTypeId,
    Short currencyId,
    String name
) {
}
