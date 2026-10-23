package com.planora.backend.currency.application;

import com.planora.backend.currency.domain.Currency;

public interface GetCurrencyByIdUseCase {
    Currency execute(int id);
}
