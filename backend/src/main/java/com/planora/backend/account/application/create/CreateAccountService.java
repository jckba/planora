package com.planora.backend.account.application.create;

import com.planora.backend.account.application.AccountReferenceValidator;
import com.planora.backend.account.domain.Account;
import com.planora.backend.account.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateAccountService implements CreateAccountUseCase{

    private final AccountRepository accountRepository;

    private final AccountReferenceValidator accountReferenceValidator;

    public CreateAccountService(AccountRepository accountRepository, AccountReferenceValidator accountReferenceValidator) {
        this.accountRepository = accountRepository;
        this.accountReferenceValidator = accountReferenceValidator;
    }

    @Override
    public Account execute(UUID userId, CreateAccountCommand command) {

        accountReferenceValidator.validate(command.accountTypeId(), command.currencyId());

        Account account = Account.create(
            userId,
            command.accountTypeId(),
            command.currencyId(),
            command.name()
        );
        return accountRepository.save(account);
    }
}
