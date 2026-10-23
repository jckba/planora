package com.planora.backend.currency.api;

import com.planora.backend.currency.api.dto.CurrencyResponse;
import com.planora.backend.currency.application.GetCurrenciesService;
import com.planora.backend.currency.application.GetCurrenciesUseCase;
import com.planora.backend.currency.application.GetCurrencyByIdUseCase;
import com.planora.backend.currency.domain.Currency;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    private final GetCurrencyByIdUseCase getCurrencyByIdUseCase;
    private final GetCurrenciesUseCase getCurrenciesUseCase;

    public CurrencyController(GetCurrencyByIdUseCase getCurrencyByIdUseCase, GetCurrenciesUseCase getCurrenciesUseCase) {
        this.getCurrencyByIdUseCase = getCurrencyByIdUseCase;
        this.getCurrenciesUseCase = getCurrenciesUseCase;
    }

    @GetMapping("/{id}")
    public CurrencyResponse getCurrencyById(@PathVariable int id) {
        return toResponse(getCurrencyByIdUseCase.execute(id));
    }

    @GetMapping
    public List<CurrencyResponse> getCurrencies() {
        return getCurrenciesUseCase.execute()
            .stream().map(this::toResponse)
            .toList();
    }

    private CurrencyResponse toResponse(Currency currency) {
        return new CurrencyResponse(
            currency.id(),
            currency.code(),
            currency.name(),
            currency.symbol(),
            currency.decimalPlaces()
        );
    }
}
