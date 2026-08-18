package com.planora.backend.account.repository.jooq;

import com.planora.backend.account.domain.Account;
import com.planora.backend.account.repository.AccountRepository;
import com.planora.backend.common.exception.OptimisticLockException;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.persistence.jooq.tables.records.AccountRecord;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static com.planora.persistence.jooq.tables.Account.ACCOUNT;
import static com.planora.persistence.jooq.tables.AppUser.APP_USER;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JooqAccountRepositoryTest {
    private final AccountRepository accountRepository;
    private final DSLContext dsl;

    @Autowired
    public JooqAccountRepositoryTest(AccountRepository accountRepository, DSLContext dsl) {
        this.accountRepository = accountRepository;
        this.dsl = dsl;
    }

    private void assertInstantEquals(
        Instant expected,
        Instant actual
    ) {
        long difference = Math.abs(
            ChronoUnit.NANOS.between(expected, actual)
        );

        assertTrue(
            difference <= 1_000,
            "Expected: " + expected + " but was: " + actual
        );
    }

    private UUID createUserForTest() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        OffsetDateTime nowOffset = now.atOffset(ZoneOffset.UTC);

        dsl.insertInto(APP_USER)
            .set(APP_USER.ID, userId)
            .set(APP_USER.USERNAME, "testuser-" + userId)
            .set(APP_USER.EMAIL, "test-" + userId + "@planora.test")
            .set(APP_USER.PASSWORD_HASH, "test-password")
            .set(APP_USER.FIRST_NAME, "Test")
            .set(APP_USER.LAST_NAME, "User")
            .set(APP_USER.PRIMARY_CURRENCY_ID, (short) 1)
            .set(APP_USER.PASSWORD_UPDATED_AT, nowOffset)
            .set(APP_USER.CREATED_AT, nowOffset)
            .set(APP_USER.UPDATED_AT, nowOffset)
            .set(APP_USER.VERSION, 0)
            .execute();
        return userId;
    }

    private Account createAccountForTest() {
        UUID userId = createUserForTest();
        Account account = Account.create(
            userId,
            (short) 1,
            (short) 1,
            "Test Account"
        );

        return accountRepository.save(account);
    }

    @Test
    @Transactional
    void shouldSaveAccount() {
        UUID userId = createUserForTest();

        Account account = Account.create(
            userId,
            (short) 1,
            (short) 1,
            "Test Account"
        );

        Account savedAccount = accountRepository.save(account);

        AccountRecord record = dsl.selectFrom(ACCOUNT)
            .where(ACCOUNT.ID.eq(savedAccount.getId()))
            .fetchOne();

        assertNotNull(record);

        assertEquals(account.getId(), record.getId());
        assertEquals(account.getUserId(), record.getUserId());
        assertEquals(
            account.getAccountTypeId(),
            record.getAccountTypeId()
        );
        assertEquals(
            account.getCurrencyId(),
            record.getCurrencyId()
        );
        assertEquals(
            0,
            account.getBalance().compareTo(record.getBalance())
        );
        assertEquals(
            account.getVersion(),
            record.getVersion()
        );

        assertEquals(
            account.getName(),
            record.getName()
        );
        assertInstantEquals(
            account.getCreatedAt(),
            record.getCreatedAt().toInstant()
        );

        assertInstantEquals(
            account.getUpdatedAt(),
            record.getUpdatedAt().toInstant()
        );
    }

    @Test
    @Transactional
    void shouldFindAccountById() {
        Account account = createAccountForTest();

        Optional<Account> result =
            accountRepository.findById(account.getId());

        assertTrue(result.isPresent());

        Account found = result.orElseThrow();

        assertEquals(account.getId(), found.getId());
        assertEquals(account.getUserId(), found.getUserId());
        assertEquals(account.getAccountTypeId(), found.getAccountTypeId());
        assertEquals(account.getCurrencyId(), found.getCurrencyId());
        assertEquals(account.getName(), found.getName());

        assertEquals(
            0,
            account.getBalance().compareTo(found.getBalance())
        );

        assertInstantEquals(
            account.getCreatedAt(),
            found.getCreatedAt()
        );

        assertInstantEquals(
            account.getUpdatedAt(),
            found.getUpdatedAt()
        );

        assertEquals(account.getVersion(), found.getVersion());
    }
    @Test
    @Transactional
    void shouldReturnEmptyWhenAccountDoesNotExist() {
        Optional<Account> result =
            accountRepository.findById(UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    void shouldFindAccountByIdAndUserId() {
        Account account = createAccountForTest();

        Optional<Account> result =
            accountRepository.findByIdAndUserId(
                account.getId(),
                account.getUserId()
            );

        assertTrue(result.isPresent());

        Account found = result.orElseThrow();

        assertEquals(account.getId(), found.getId());
        assertEquals(account.getUserId(), found.getUserId());
        assertEquals(account.getName(), found.getName());
    }

    @Test
    @Transactional
    void shouldNotFindAccountWhenUserDoesNotOwnIt() {
        Account account = createAccountForTest();

        UUID anotherUserId = createUserForTest();

        Optional<Account> result =
            accountRepository.findByIdAndUserId(
                account.getId(),
                anotherUserId
            );

        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    void shouldFindAccountsByUserId() {
        UUID userId = createUserForTest();

        Account firstAccount = accountRepository.save(
            Account.create(
                userId,
                (short) 1,
                (short) 1,
                "Checking"
            )
        );

        Account secondAccount = accountRepository.save(
            Account.create(
                userId,
                (short) 1,
                (short) 1,
                "Savings"
            )
        );

        PageResult<Account> result =
            accountRepository.findByUserId(
                userId,
                new PageRequest(0, 10)
            );

        assertEquals(2, result.content().size());
        assertEquals(2, result.totalElements());
        assertEquals(0, result.page());
        assertEquals(10, result.size());

        assertEquals(
            firstAccount.getId(),
            result.content().get(0).getId()
        );

        assertEquals(
            secondAccount.getId(),
            result.content().get(1).getId()
        );
    }

    @Test
    @Transactional
    void shouldNotFindAccountsFromAnotherUser() {
        UUID userId = createUserForTest();
        UUID anotherUserId = createUserForTest();

        accountRepository.save(
            Account.create(
                anotherUserId,
                (short) 1,
                (short) 1,
                "Other Account"
            )
        );

        PageResult<Account> result =
            accountRepository.findByUserId(
                userId,
                new PageRequest(0, 10)
            );

        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
    }

    @Test
    @Transactional
    void shouldRejectUpdateWhenVersionIsStale() {
        Account account = createAccountForTest();

        Account staleAccount = Account.restore(
            account.getId(),
            account.getUserId(),
            account.getAccountTypeId(),
            account.getCurrencyId(),
            account.getName(),
            account.getBalance(),
            account.getCreatedAt(),
            account.getUpdatedAt(),
            account.getVersion()
        );

        account.update(
            (short) 2,
            (short) 2,
            "Updated Account"
        );

        accountRepository.update(account);

        staleAccount.update(
            (short) 3,
            (short) 3,
            "Stale Account"
        );

        assertThrows(
            OptimisticLockException.class,
            () -> accountRepository.update(staleAccount)
        );
    }

    @Test
    @Transactional
    void shouldDeleteAccount() {
        Account account = createAccountForTest();

        accountRepository.delete(
            account.getId(),
            account.getUserId()
        );

        Optional<Account> result =
            accountRepository.findById(
                account.getId()
            );

        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    void shouldNotDeleteAccountOwnedByAnotherUser() {
        Account account = createAccountForTest();

        UUID anotherUserId = createUserForTest();

        accountRepository.delete(
            account.getId(),
            anotherUserId
        );

        Optional<Account> result =
            accountRepository.findById(
                account.getId()
            );

        assertTrue(result.isPresent());
    }


}
