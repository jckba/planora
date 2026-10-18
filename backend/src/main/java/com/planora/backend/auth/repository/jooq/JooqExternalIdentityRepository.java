package com.planora.backend.auth.repository.jooq;

import com.planora.backend.auth.domain.ExternalIdentity;
import com.planora.backend.auth.repository.ExternalIdentityRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static com.planora.persistence.jooq.tables.ExternalIdentity.EXTERNAL_IDENTITY;

@Repository
public class JooqExternalIdentityRepository implements ExternalIdentityRepository {

    private final DSLContext dsl;

    public JooqExternalIdentityRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Optional<UUID> findUserIdByGoogleSubject(String googleSubject) {
        return dsl.select(EXTERNAL_IDENTITY.USER_ID)
            .from(EXTERNAL_IDENTITY)
            .where(EXTERNAL_IDENTITY.PROVIDER_SUBJECT.eq("GOOGLE"))
                .and(EXTERNAL_IDENTITY.PROVIDER_SUBJECT.eq(googleSubject))
                .fetchOptional(EXTERNAL_IDENTITY.USER_ID);
    }

    @Override
    public boolean existsByUserIdAndProvider(UUID userId, String provider) {
        return dsl.fetchExists(
            dsl.selectOne()
                .from(EXTERNAL_IDENTITY)
                .where(EXTERNAL_IDENTITY.USER_ID.eq(userId))
                .and(EXTERNAL_IDENTITY.PROVIDER.eq(provider))
        );
    }

    @Override
    public ExternalIdentity save(ExternalIdentity externalIdentity) {
        dsl.insertInto(EXTERNAL_IDENTITY)
            .set(EXTERNAL_IDENTITY.ID, externalIdentity.id())
            .set(EXTERNAL_IDENTITY.USER_ID, externalIdentity.userId())
            .set(EXTERNAL_IDENTITY.PROVIDER, externalIdentity.provider())
            .set(EXTERNAL_IDENTITY.PROVIDER_SUBJECT, externalIdentity.providerSubject())
            .set(EXTERNAL_IDENTITY.CREATED_AT, OffsetDateTime.ofInstant(externalIdentity.createdAt(), ZoneOffset.UTC))
            .execute();

        return externalIdentity;
    }
}
