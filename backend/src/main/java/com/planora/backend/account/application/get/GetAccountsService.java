package com.planora.backend.account.application.get;

import com.planora.backend.account.domain.Account;
import com.planora.backend.account.repository.AccountRepository;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.common.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetAccountsService implements GetAccountsUseCase{

    private final AccountRepository accountRepository;
    private final CurrentUser currentUser;

    public GetAccountsService(AccountRepository accountRepository, CurrentUser currentUser) {
        this.accountRepository = accountRepository;
        this.currentUser = currentUser;
    }

    @Override
    public PageResult<Account> execute(PageRequest pageRequest) {
        UUID userId = currentUser.userId();
        return accountRepository.findByUserId(
            userId,
            pageRequest);
    }
}
