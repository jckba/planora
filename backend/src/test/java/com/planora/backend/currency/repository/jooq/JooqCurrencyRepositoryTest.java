package com.planora.backend.currency.repository.jooq;

import com.planora.backend.currency.repository.CurrencyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JooqCurrencyRepositoryTest {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Test
    void shouldReturnTrueWhenCurrencyExists() {
        assertTrue(
            currencyRepository.existsById((short) 1)
        );
    }

    @Test
    void shouldReturnFalseWhenCurrencyDoesNotExist() {
        assertFalse(
            currencyRepository.existsById((short) 999)
        );
    }
}
