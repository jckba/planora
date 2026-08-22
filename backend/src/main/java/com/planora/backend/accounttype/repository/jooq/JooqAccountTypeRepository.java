package com.planora.backend.accounttype.repository.jooq;

import com.planora.backend.accounttype.repository.AccountTypeRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static com.planora.persistence.jooq.tables.AccountType.ACCOUNT_TYPE;

@Repository
public class JooqAccountTypeRepository implements AccountTypeRepository {

    private final DSLContext dsl;

    public JooqAccountTypeRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public boolean existsById(Short id) {
        return dsl.fetchExists(
            dsl.selectOne()
                .from(ACCOUNT_TYPE)
                .where(ACCOUNT_TYPE.ID.eq(id))
        );
    }
}
