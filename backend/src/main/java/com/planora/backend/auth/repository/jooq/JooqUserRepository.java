package com.planora.backend.auth.repository.jooq;

import com.planora.backend.auth.domain.User;
import com.planora.backend.auth.repository.UserRepository;
import com.planora.persistence.jooq.tables.records.AppUserRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static com.planora.persistence.jooq.tables.AppUser.APP_USER;

@Repository
public class JooqUserRepository implements UserRepository {

    private final DSLContext dsl;

    public JooqUserRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public User save(User user) {
        dsl.insertInto(APP_USER)
            .set(APP_USER.ID, user.getId())
            .set(APP_USER.USERNAME, user.getUsername())
            .set(APP_USER.EMAIL, user.getEmail())
            .set(APP_USER.PASSWORD_HASH, user.getPasswordHash())
            .set(APP_USER.FIRST_NAME, user.getFirstName())
            .set(APP_USER.LAST_NAME, user.getLastName())
            .set(APP_USER.PRIMARY_CURRENCY_ID, user.getPrimaryCurrencyId())
            .set(APP_USER.PASSWORD_UPDATED_AT, OffsetDateTime.ofInstant(user.getPasswordUpdatedAt(), ZoneOffset.UTC))
            .set(APP_USER.CREATED_AT, OffsetDateTime.ofInstant(user.getCreatedAt(), ZoneOffset.UTC))
            .set(APP_USER.UPDATED_AT, OffsetDateTime.ofInstant(user.getUpdatedAt(), ZoneOffset.UTC))
            .set(APP_USER.VERSION, user.getVersion())
            .execute();
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        AppUserRecord record = dsl.selectFrom(APP_USER)
            .where(APP_USER.ID.eq(id))
            .fetchOne();
        return Optional.ofNullable(record)
            .map(this::mapToUser);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        AppUserRecord record = dsl.selectFrom(APP_USER)
            .where(APP_USER.EMAIL.eq(email))
            .fetchOne();
        return Optional.ofNullable(record)
            .map(this::mapToUser);
    }

    @Override
    public boolean existsByEmail(String email) {
        return dsl.fetchExists(
            dsl.selectOne()
            .from(APP_USER).where(APP_USER.EMAIL.eq(email))
        );
    }

    @Override
    public boolean existsByUsername(String username) {
        return dsl.fetchExists(
            dsl.selectOne()
                .from(APP_USER).where(APP_USER.USERNAME.eq(username))
        );
    }

    private User mapToUser(AppUserRecord record) {
        return User.restore(
            record.getId(),
            record.getUsername(),
            record.getEmail(),
            record.getPasswordHash(),
            record.getFirstName(),
            record.getLastName(),
            record.getPrimaryCurrencyId(),
            record.getPasswordUpdatedAt().toInstant(),
            record.getCreatedAt().toInstant(),
            record.getUpdatedAt().toInstant(),
            record.getVersion()
        );
    }
}
