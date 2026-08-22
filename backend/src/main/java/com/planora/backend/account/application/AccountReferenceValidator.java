package com.planora.backend.account.application;

import com.planora.backend.accounttype.repository.AccountTypeRepository;
import com.planora.backend.common.exception.InvalidAccountReferenceException;
import com.planora.backend.currency.repository.CurrencyRepository;
import org.springframework.stereotype.Component;

@Component
public class AccountReferenceValidator {

    private final AccountTypeRepository accountTypeRepository;
    private final CurrencyRepository currencyRepository;

    public AccountReferenceValidator(AccountTypeRepository accountTypeRepository, CurrencyRepository currencyRepository) {
        this.accountTypeRepository = accountTypeRepository;
        this.currencyRepository = currencyRepository;
    }


    public void validate (Short accountTypeId, Short currencyId) {
        if(!accountTypeRepository.existsById(accountTypeId)) {
            throw new InvalidAccountReferenceException(
                "Account type not found"
            );
        }

        if(!currencyRepository.existsById(currencyId)) {
            throw new InvalidAccountReferenceException(
                "Currency not found"
            );
        }
    }

}
