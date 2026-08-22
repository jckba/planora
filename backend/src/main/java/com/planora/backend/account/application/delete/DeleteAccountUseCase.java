package com.planora.backend.account.application.delete;

import java.util.UUID;

public interface DeleteAccountUseCase {

    void execute(UUID userId, UUID accountId);
}
