package com.planora.backend.currency.application;

import com.planora.backend.currency.domain.Currency;
import com.planora.backend.currency.repository.jooq.JooqCurrencyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetCurrenciesService implements GetCurrenciesUseCase{

    private final JooqCurrencyRepository currencyRepository;

    public GetCurrenciesService(JooqCurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @Override
    public List<Currency> execute() {
        return currencyRepository.findAll();
    }
}
