package com.planora.backend.currency.repository.jooq;

import com.planora.backend.currency.domain.Currency;
import com.planora.backend.currency.repository.CurrencyRepository;
import com.planora.persistence.jooq.tables.records.CurrencyRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.planora.persistence.jooq.tables.Currency.CURRENCY;

@Repository
public class JooqCurrencyRepository implements CurrencyRepository {

    private final DSLContext dsl;

    public JooqCurrencyRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Currency> findById(int id) {
        CurrencyRecord currencyRecord = dsl.selectFrom(CURRENCY)
            .where(CURRENCY.ID.eq((short) id))
            .fetchOne();

        return Optional.ofNullable(currencyRecord)
            .map(this::mapToCurrency);
    }

    @Override
    public List<Currency> findAll() {
        return dsl.selectFrom(CURRENCY)
            .orderBy(CURRENCY.CODE.asc())
            .fetch(this::mapToCurrency);
    }

    @Override
    public boolean existsById(Short id) {
        return dsl.fetchExists(
            dsl.selectOne()
                .from(CURRENCY)
                .where(CURRENCY.ID.eq(id))
        );
    }

    private Currency mapToCurrency(CurrencyRecord currencyRecord) {
        return new Currency(
            currencyRecord.getId(),
            currencyRecord.getCode(),
            currencyRecord.getName(),
            currencyRecord.getSymbol(),
            currencyRecord.getDecimalPlaces()
        );

    }

}
