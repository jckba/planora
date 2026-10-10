package com.planora.backend.account.application.get;

import com.planora.backend.account.domain.Account;
import com.planora.backend.account.repository.AccountRepository;
import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.common.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetAccountByIdService implements GetAccountByIdUseCase{

    private final AccountRepository accountRepository;
    private final CurrentUser currentUser;

    public GetAccountByIdService(AccountRepository accountRepository, CurrentUser currentUser) {
        this.accountRepository = accountRepository;
        this.currentUser = currentUser;
    }

    @Override
    public Account execute(UUID accountId) {
        UUID userId = currentUser.userId();
        return accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }
}
