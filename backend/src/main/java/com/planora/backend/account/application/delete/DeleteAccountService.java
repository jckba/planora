package com.planora.backend.account.application.delete;

import com.planora.backend.account.repository.AccountRepository;
import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.common.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteAccountService implements DeleteAccountUseCase{

    private final AccountRepository accountRepository;
    private final CurrentUser currentUser;

    public DeleteAccountService(AccountRepository accountRepository, CurrentUser currentUser) {
        this.accountRepository = accountRepository;
        this.currentUser = currentUser;
    }

    @Override
    public void execute(UUID accountId) {
        UUID userId = currentUser.userId();
        accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
            );

        accountRepository.delete(accountId, userId);
    }
}
