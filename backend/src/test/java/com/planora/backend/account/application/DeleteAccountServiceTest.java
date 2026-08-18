package com.planora.backend.account.application;

import com.planora.backend.account.application.update.DeleteAccountService;
import com.planora.backend.account.domain.Account;
import com.planora.backend.account.repository.AccountRepository;
import com.planora.backend.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteAccountServiceTest
{
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private DeleteAccountService deleteAccountService;

    @Test
    void shouldDeleteAccount() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        Account account = Account.create(
            userId,
            (short) 1,
            (short) 1,
            "My Account"
        );

        when(
            accountRepository.findByIdAndUserId(
                accountId,
                userId
            )
        ).thenReturn(Optional.of(account));

        deleteAccountService.execute(
            userId,
            accountId
        );

        verify(accountRepository)
            .delete(accountId, userId);
    }

    @Test
    void shouldThrowWhenAccountDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        when(
            accountRepository.findByIdAndUserId(
                accountId,
                userId
            )
        ).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> deleteAccountService.execute(
                userId,
                accountId
            )
        );

        verify(
            accountRepository,
            never()
        ).delete(accountId, userId);
    }
}
