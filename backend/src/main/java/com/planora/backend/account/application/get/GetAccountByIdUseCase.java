package com.planora.backend.account.application.get;

import com.planora.backend.account.domain.Account;

import java.util.UUID;

public interface GetAccountByIdUseCase {
    Account execute (UUID accountId);
}
