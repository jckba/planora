package com.planora.backend.account.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {

    //Create test
    @Test
    void shouldCreateValidAccount() {
        UUID userId = UUID.randomUUID();
        Short accountTypeId = (short) 1;
        Short currencyId = (short) 1;
        String name = "My Account";

        Account account = Account.create(
            userId,
            accountTypeId,
            currencyId,
            name);

        assertNotNull(account);
        assertNotNull(account.getId());

        assertEquals(userId, account.getUserId());
        assertEquals(accountTypeId, account.getAccountTypeId());
        assertEquals(currencyId, account.getCurrencyId());
        assertEquals(name, account.getName());

        assertEquals(
            0,
            BigDecimal.ZERO.compareTo(account.getBalance())
        );

        assertNotNull(account.getCreatedAt());
        assertNotNull(account.getUpdatedAt());
        assertEquals(0, account.getVersion());
    }

    @Test
    void shouldNormalizeName() {
        Account account = Account.create(
            UUID.randomUUID(),
            (short) 1,
            (short) 1,
            "  My   Bank   Account  "
        );

        assertEquals(
            "My Bank Account",
            account.getName()
        );
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Account.create(
                UUID.randomUUID(),
                (short) 1,
                (short) 1,
                " \t "
            )
        );
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(
            NullPointerException.class,
            () -> Account.create(
                UUID.randomUUID(),
                (short) 1,
                (short) 1,
                null
            )
        );
    }

    @Test
    void shouldRejectNullUserId() {
        assertThrows(
            NullPointerException.class,
            () -> Account.create(
                null,
                (short) 1,
                (short) 1,
                "My Account"
            )
        );
    }

    @Test
    void shouldRejectNullAccountTypeId() {
        assertThrows(
            NullPointerException.class,
            () -> Account.create(
                UUID.randomUUID(),
                null,
                (short) 1,
                "My Account"
            )
        );
    }

    @Test
    void shouldRejectNullCurrencyId() {
        assertThrows(
            NullPointerException.class,
            () -> Account.create(
                UUID.randomUUID(),
                (short) 1,
                null,
                "My Account"
            )
        );
    }

    @Test
    void shouldUpdateAccount() {
        UUID userId = UUID.randomUUID();

        Account account = Account.create(
            userId,
            (short) 1,
            (short) 1,
            "My Account"
        );

        Instant originalCreatedAt = account.getCreatedAt();
        BigDecimal originalBalance = account.getBalance();

        account.update(
            (short) 2,
            (short) 2,
            "Updated Account"
        );

        assertEquals(
            (short) 2,
            account.getAccountTypeId()
        );
        assertEquals(
            (short) 2,
            account.getCurrencyId()
        );
        assertEquals(
            "Updated Account",
            account.getName()
        );

        assertEquals(
            originalBalance,
            account.getBalance()
        );

        assertEquals(
            originalCreatedAt,
            account.getCreatedAt()
        );

        assertEquals(0, account.getVersion());
        assertNotNull(account.getUpdatedAt());
    }

}
