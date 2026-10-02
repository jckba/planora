package com.planora.backend.account.application;

import com.planora.backend.account.application.get.GetAccountsService;
import com.planora.backend.account.domain.Account;
import com.planora.backend.account.repository.AccountRepository;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.common.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAccountsServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CurrentUser currentUser;
    @InjectMocks
    private GetAccountsService service;

    @Test
    void shouldGetRequestedPageForAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        PageRequest request = new PageRequest(2, 5);
        PageResult<Account> expected = new PageResult<>(
            List.of(Account.create(userId, (short) 1, (short) 2, "Savings")), 2, 5, 11
        );
        when(currentUser.userId()).thenReturn(userId);
        when(accountRepository.findByUserId(userId, request)).thenReturn(expected);

        assertSame(expected, service.execute(request));

        verify(accountRepository).findByUserId(userId, request);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void shouldReturnEmptyPageForAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        PageRequest request = new PageRequest(0, 10);
        PageResult<Account> expected = new PageResult<>(List.of(), 0, 10, 0);
        when(currentUser.userId()).thenReturn(userId);
        when(accountRepository.findByUserId(userId, request)).thenReturn(expected);

        assertSame(expected, service.execute(request));
    }
}
