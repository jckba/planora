package com.planora.backend.account.application;

import com.planora.backend.account.application.get.GetAccountByIdService;
import com.planora.backend.account.domain.Account;
import com.planora.backend.account.repository.AccountRepository;
import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.common.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAccountByIdServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private GetAccountByIdService getAccountByIdService;

    @Test
    void shouldFindAccountById() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        Account account = Account.create(
            userId,
            (short) 1,
            (short) 1,
            "My Account"
        );

        when(accountRepository.findByIdAndUserId(accountId, userId))
            .thenReturn(Optional.of(account));
        when(currentUser.userId()).thenReturn(userId);

        Account result = getAccountByIdService.execute(accountId);

        assertSame(account, result);

        verify(accountRepository).findByIdAndUserId(accountId, userId);
    }

    @Test
    void shouldThrowWhenAccountDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        when(accountRepository.findByIdAndUserId(accountId, userId))
            .thenReturn(Optional.empty());
        when(currentUser.userId()).thenReturn(userId);

        assertThrows(
            ResourceNotFoundException.class,
            () -> getAccountByIdService.execute(accountId)
        );

        verify(accountRepository)
            .findByIdAndUserId(accountId, userId);
    }

    @Test
    void shouldThrowWhenAccountDoesNotBelongToUser() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        when(accountRepository.findByIdAndUserId(accountId, userId))
            .thenReturn(Optional.empty());
        when(currentUser.userId()).thenReturn(userId);

        assertThrows(
            ResourceNotFoundException.class,
            () -> getAccountByIdService.execute(accountId)
        );

        verify(accountRepository)
            .findByIdAndUserId(accountId, userId);
    }

}
