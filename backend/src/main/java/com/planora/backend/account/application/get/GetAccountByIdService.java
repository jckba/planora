package com.planora.backend.account.application.get;

import com.planora.backend.account.domain.Account;
import com.planora.backend.account.repository.AccountRepository;
import com.planora.backend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetAccountByIdService implements GetAccountByIdUseCase{

    private final AccountRepository accountRepository;

    public GetAccountByIdService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account execute(UUID userId, UUID accountId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }
}
