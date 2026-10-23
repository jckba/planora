package com.planora.backend.currency.application;

import com.planora.backend.currency.domain.Currency;

import java.util.List;

public interface GetCurrenciesUseCase {

    List<Currency> execute();
}
