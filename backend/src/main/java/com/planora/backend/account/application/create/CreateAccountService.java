package com.planora.backend.account.application.create;

import com.planora.backend.account.domain.Account;
import com.planora.backend.account.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateAccountService implements CreateAccountUseCase{

    private final AccountRepository accountRepository;

    public CreateAccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account execute(UUID userId, CreateAccountCommand command) {
        Account account = Account.create(
            userId,
            command.accountTypeId(),
            command.currencyId(),
            command.name()
        );
        return accountRepository.save(account);
    }
}
