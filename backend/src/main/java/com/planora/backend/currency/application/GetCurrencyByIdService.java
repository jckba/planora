package com.planora.backend.currency.application;

import com.planora.backend.currency.domain.Currency;
import com.planora.backend.currency.repository.jooq.JooqCurrencyRepository;
import org.springframework.stereotype.Service;

import java.lang.module.ResolutionException;

@Service
public class GetCurrencyByIdService implements GetCurrencyByIdUseCase {

    private final JooqCurrencyRepository currencyRepository;

    public GetCurrencyByIdService(JooqCurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @Override
    public Currency execute(int id) {
        return currencyRepository.findById(id)
            .orElseThrow(
                () -> new ResolutionException("Currency not found")
            );
    }
}
