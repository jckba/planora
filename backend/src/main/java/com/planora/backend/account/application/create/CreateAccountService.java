package com.planora.backend.account.application.create;

import com.planora.backend.account.application.AccountReferenceValidator;
import com.planora.backend.account.domain.Account;
import com.planora.backend.account.repository.AccountRepository;
import com.planora.backend.common.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateAccountService implements CreateAccountUseCase{

    private final AccountRepository accountRepository;
    private final AccountReferenceValidator accountReferenceValidator;
    private final CurrentUser currentUser;

    public CreateAccountService(AccountRepository accountRepository, AccountReferenceValidator accountReferenceValidator, CurrentUser currentUser) {
        this.accountRepository = accountRepository;
        this.accountReferenceValidator = accountReferenceValidator;
        this.currentUser = currentUser;
    }

    @Override
    public Account execute(CreateAccountCommand command) {
    UUID userId = currentUser.userId();
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
