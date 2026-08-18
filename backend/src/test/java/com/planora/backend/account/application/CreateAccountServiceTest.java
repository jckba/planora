package com.planora.backend.account.application;

import com.planora.backend.account.application.create.CreateAccountCommand;
import com.planora.backend.account.application.create.CreateAccountService;
import com.planora.backend.account.domain.Account;
import com.planora.backend.account.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CreateAccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @InjectMocks
    private CreateAccountService createAccountService;

    @Test
    void shouldCreateAccount() {
        UUID userId = UUID.randomUUID();

        CreateAccountCommand command = new CreateAccountCommand(
            userId,
            (short) 1,
            (short) 1,
            "My Account"
        );

        Account savedAccount = Account.create(
            userId,
            command.accountTypeId(),
            command.currencyId(),
            command.name()
        );

        when(accountRepository.save(any(Account.class)))
            .thenReturn(savedAccount);

        Account result = createAccountService.execute(userId, command);

        assertSame(savedAccount, result);

        ArgumentCaptor<Account> captor =
            ArgumentCaptor.forClass(Account.class);

        verify(accountRepository).save(captor.capture());

        Account createdAccount = captor.getValue();

        assertEquals(userId, createdAccount.getUserId());
        assertEquals(
            command.accountTypeId(),
            createdAccount.getAccountTypeId()
        );
        assertEquals(
            command.currencyId(),
            createdAccount.getCurrencyId()
        );
        assertEquals(
            command.name(),
            createdAccount.getName()
        );

        assertEquals(
            0,
            BigDecimal.ZERO.compareTo(createdAccount.getBalance())
        );

        assertNotNull(createdAccount.getId());
        assertNotNull(createdAccount.getCreatedAt());
        assertNotNull(createdAccount.getUpdatedAt());
        assertEquals(0, createdAccount.getVersion());
    }

    @Test
    void shouldRejectInvalidAccount() {
        UUID userId = UUID.randomUUID();

        CreateAccountCommand command = new CreateAccountCommand(
            userId,
            (short) 1,
            (short) 1,
            "   "
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> createAccountService.execute(userId, command)
        );

        verifyNoInteractions(accountRepository);
    }



}
