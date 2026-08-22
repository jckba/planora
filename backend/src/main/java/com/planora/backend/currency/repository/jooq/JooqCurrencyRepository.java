package com.planora.backend.currency.repository.jooq;

import com.planora.backend.currency.repository.CurrencyRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static com.planora.persistence.jooq.tables.Currency.CURRENCY;

@Repository
public class JooqCurrencyRepository implements CurrencyRepository {

    private final DSLContext dsl;

    public JooqCurrencyRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public boolean existsById(Short id) {
        return dsl.fetchExists(
            dsl.selectOne()
                .from(CURRENCY)
                .where(CURRENCY.ID.eq(id))
        );
    }

}
