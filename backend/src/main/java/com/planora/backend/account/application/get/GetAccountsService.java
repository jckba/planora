package com.planora.backend.account.application.get;

import com.planora.backend.account.domain.Account;
import com.planora.backend.account.repository.AccountRepository;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetAccountsService implements GetAccountsUseCase{

    private final AccountRepository accountRepository;

    public GetAccountsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public PageResult<Account> execute(UUID userId, PageRequest pageRequest) {
        return accountRepository.findByUserId(
            userId,
            pageRequest);
    }
}
