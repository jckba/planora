package com.planora.backend.account.application.create;

import com.planora.backend.account.domain.Account;

import java.util.UUID;

public interface CreateAccountUseCase {
    Account execute(CreateAccountCommand command);
}
