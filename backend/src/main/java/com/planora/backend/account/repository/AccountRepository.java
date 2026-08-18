package com.planora.backend.account.repository;

import com.planora.backend.account.domain.Account;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    Account save(Account account);

    Account update(Account account);

    void delete(UUID accountId, UUID userId);

    Optional<Account> findById(UUID id);

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    PageResult<Account> findByUserId(UUID userId, PageRequest pageRequest);
}

