package com.planora.backend.account.application.delete;

import com.planora.backend.account.repository.AccountRepository;
import com.planora.backend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteAccountService implements DeleteAccountUseCase{

    private final AccountRepository accountRepository;

    public DeleteAccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void execute(UUID userId, UUID accountId) {
        accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
            );

        accountRepository.delete(accountId, userId);
    }
}
