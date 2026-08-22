package com.planora.backend.account.application.update;

import com.planora.backend.account.application.AccountReferenceValidator;
import com.planora.backend.account.domain.Account;
import com.planora.backend.account.repository.AccountRepository;
import com.planora.backend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UpdateAccountService implements UpdateAccountUseCase{

    private final AccountRepository accountRepository;
    private final AccountReferenceValidator accountReferenceValidator;

    public UpdateAccountService(AccountRepository accountRepository, AccountReferenceValidator accountReferenceValidator) {
        this.accountRepository = accountRepository;
        this.accountReferenceValidator = accountReferenceValidator;
    }

    @Override
    public Account execute(UpdateAccountCommand command) {

        accountReferenceValidator.validate(command.accountTypeId(), command.currencyId());

        Account account = accountRepository.findByIdAndUserId(
            command.accountId(),
            command.userId()
        ).orElseThrow(
            () -> new ResourceNotFoundException("Account not found")
        );

        account.update(
            command.accountTypeId(),
            command.currencyId(),
            command.name()
        );

        return accountRepository.update(account);

    }
}
