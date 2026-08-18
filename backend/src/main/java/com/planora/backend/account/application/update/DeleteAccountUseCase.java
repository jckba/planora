package com.planora.backend.account.application.update;

import java.util.UUID;

public interface DeleteAccountUseCase {

    void execute(UUID userId, UUID accountId);
}
