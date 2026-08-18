package com.planora.backend.account.api.dto;

public record UpdateAccountRequest(
    Short accountTypeId,
    Short currencyId,
    String name
) {
}
