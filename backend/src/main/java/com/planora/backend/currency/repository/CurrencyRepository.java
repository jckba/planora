package com.planora.backend.currency.repository;

import com.planora.backend.currency.domain.Currency;

import java.util.List;
import java.util.Optional;

public interface CurrencyRepository {

    Optional<Currency> findById(int id);

    List<Currency> findAll();

    boolean existsById(Short id);
}
