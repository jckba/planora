package com.planora.backend.account.application;

import com.planora.backend.account.application.update.UpdateAccountCommand;
import com.planora.backend.account.application.update.UpdateAccountService;
import com.planora.backend.account.domain.Account;
import com.planora.backend.account.repository.AccountRepository;
import com.planora.backend.common.exception.InvalidAccountReferenceException;
import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.common.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateAccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountReferenceValidator accountReferenceValidator;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private UpdateAccountService updateAccountService;

    @Test
    void shouldUpdateAccount() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        Account account = Account.create(
            userId,
            (short) 1,
            (short) 1,
            "My Account"
        );

        UpdateAccountCommand command =
            new UpdateAccountCommand(
                accountId,
                (short) 2,
                (short) 2,
                "Updated Account"
            );

        when(
            accountRepository.findByIdAndUserId(
                command.accountId(),
                userId
            )
        ).thenReturn(Optional.of(account));
        when(currentUser.userId()).thenReturn(userId);

        when(
            accountRepository.update(account)
        ).thenReturn(account);

        Account result =
            updateAccountService.execute(command);

        assertSame(account, result);

        verify(accountReferenceValidator)
            .validate(
                command.accountTypeId(),
                command.currencyId()
            );

        verify(accountRepository)
            .findByIdAndUserId(
                command.accountId(),
                userId
            );

        verify(accountRepository)
            .update(account);

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
    }

    @Test
    void shouldRejectInvalidReferences() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        UpdateAccountCommand command =
            new UpdateAccountCommand(
                accountId,
                (short) 99,
                (short) 1,
                "Updated Account"
            );
        when(currentUser.userId()).thenReturn(userId);

        doThrow(
            new InvalidAccountReferenceException(
                "Account type not found"
            )
        )
            .when(accountReferenceValidator)
            .validate(
                command.accountTypeId(),
                command.currencyId()
            );

        assertThrows(
            InvalidAccountReferenceException.class,
            () -> updateAccountService.execute(command)
        );

        verifyNoInteractions(accountRepository);
    }

    @Test
    void shouldThrowWhenAccountDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        UpdateAccountCommand command =
            new UpdateAccountCommand(
                accountId,
                (short) 1,
                (short) 1,
                "Updated Account"
            );

        when(
            accountRepository.findByIdAndUserId(
                command.accountId(),
                userId
            )
        ).thenReturn(Optional.empty());
        when(currentUser.userId()).thenReturn(userId);

        assertThrows(
            ResourceNotFoundException.class,
            () -> updateAccountService.execute(command)
        );

        verify(accountRepository)
            .findByIdAndUserId(
                command.accountId(),
                userId
            );

        verify(
            accountRepository,
            never()
        ).update(any(Account.class));
    }
}
