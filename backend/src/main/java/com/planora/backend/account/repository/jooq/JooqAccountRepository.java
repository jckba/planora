package com.planora.backend.account.repository.jooq;

import com.planora.backend.account.domain.Account;
import com.planora.backend.account.repository.AccountRepository;
import com.planora.backend.common.exception.OptimisticLockException;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.persistence.jooq.tables.records.AccountRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.planora.persistence.jooq.tables.Account.ACCOUNT;

@Repository
public class JooqAccountRepository implements AccountRepository {

    private final DSLContext dsl;

    public JooqAccountRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Account save(Account account) {
        dsl.insertInto(ACCOUNT)
            .set(ACCOUNT.ID, account.getId())
            .set(ACCOUNT.USER_ID, account.getUserId())
            .set(ACCOUNT.ACCOUNT_TYPE_ID, account.getAccountTypeId())
            .set(ACCOUNT.CURRENCY_ID, account.getCurrencyId())
            .set(ACCOUNT.NAME, account.getName())
            .set(ACCOUNT.BALANCE, account.getBalance())
            .set(ACCOUNT.CREATED_AT, OffsetDateTime.ofInstant(account.getCreatedAt(), ZoneOffset.UTC))
            .set(ACCOUNT.UPDATED_AT, OffsetDateTime.ofInstant(account.getUpdatedAt(), ZoneOffset.UTC))
            .set(ACCOUNT.VERSION, account.getVersion())
            .execute();

        return account;
    }

    @Override
    public Account update(Account account) {
        int updatedRows = dsl.update(ACCOUNT)
            .set(
                ACCOUNT.ACCOUNT_TYPE_ID,
                account.getAccountTypeId()
            )
            .set(
                ACCOUNT.CURRENCY_ID,
                account.getCurrencyId()
            )
            .set(
                ACCOUNT.NAME,
                account.getName()
            )
            .set(
                ACCOUNT.UPDATED_AT,
                OffsetDateTime.ofInstant(
                    account.getUpdatedAt(),
                    ZoneOffset.UTC
                )
            )
            .set(
                ACCOUNT.VERSION,
                ACCOUNT.VERSION.plus(1)
            )
            .where(
                ACCOUNT.ID.eq(account.getId())
            )
            .and(
                ACCOUNT.USER_ID.eq(account.getUserId())
            )
            .and(
                ACCOUNT.VERSION.eq(account.getVersion())
            )
            .execute();

        if (updatedRows == 0) {
            throw new OptimisticLockException(
                "Account was modified by another transaction"
            );
        }

        account.setVersion(account.getVersion() + 1);

        return account;
    }

    @Override
    public void delete(UUID accountId, UUID userId) {
        dsl.deleteFrom(ACCOUNT)
            .where(ACCOUNT.ID.eq(accountId))
            .and(ACCOUNT.USER_ID.eq(userId))
            .execute();
    }

    @Override
    public Optional<Account> findById(UUID id) {
        AccountRecord record = dsl.selectFrom(ACCOUNT)
            .where(ACCOUNT.ID.eq(id))
            .fetchOne();
        return Optional.ofNullable(record).map(this::mapToAccount);
    }

    @Override
    public Optional<Account> findByIdAndUserId(UUID id, UUID userId) {
        AccountRecord record = dsl.selectFrom(ACCOUNT)
            .where(ACCOUNT.ID.eq(id))
            .and(ACCOUNT.USER_ID.eq(userId))
            .fetchOne();
        return Optional.ofNullable(record).map(this::mapToAccount);
    }

    @Override
    public PageResult<Account> findByUserId(UUID userId, PageRequest pageRequest) {
        int offset =
            pageRequest.page() * pageRequest.size();

        List<Account> accounts =
            dsl.selectFrom(ACCOUNT)
                .where(ACCOUNT.USER_ID.eq(userId))
                .orderBy(ACCOUNT.NAME.asc())
                .limit(pageRequest.size())
                .offset(offset)
                .fetch()
                .map(this::mapToAccount);

        Long totalElements =
            dsl.selectCount()
                .from(ACCOUNT)
                .where(ACCOUNT.USER_ID.eq(userId))
                .fetchOne(0, Long.class);

        return new PageResult<>(
            accounts,
            pageRequest.page(),
            pageRequest.size(),
            totalElements != null ? totalElements : 0L
        );
    }

    private Account mapToAccount(AccountRecord record) {
        return Account.restore(
            record.getId(),
            record.getUserId(),
            record.getAccountTypeId(),
            record.getCurrencyId(),
            record.getName(),
            record.getBalance(),
            record.getCreatedAt().toInstant(),
            record.getUpdatedAt().toInstant(),
            record.getVersion()

        );
    }
}
