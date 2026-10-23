package com.planora.backend.currency.domain;

public record Currency(
    int id,
    String code,
    String name,
    String symbol,
    int decimalPlaces
) {
}
