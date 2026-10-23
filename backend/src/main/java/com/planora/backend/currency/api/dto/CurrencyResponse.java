package com.planora.backend.currency.api.dto;

public record CurrencyResponse(
    int id,
    String code,
    String name,
    String symbol,
    int decimalPlaces
) {
}
