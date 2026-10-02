package com.planora.backend.account.application.update;

import com.planora.backend.account.domain.Account;

public interface UpdateAccountUseCase {
    Account execute(UpdateAccountCommand command);
}
