package com.planora.backend.account.application.get;

import com.planora.backend.account.domain.Account;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;

import java.util.UUID;

public interface GetAccountsUseCase {
    PageResult<Account> execute(PageRequest pageRequest);
}
