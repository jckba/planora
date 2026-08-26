package com.planora.backend.purchase.repository;

import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.purchase.domain.Purchase;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository {

    Purchase save(Purchase purchase);

    Optional<Purchase> findById(UUID id);

    Optional<Purchase> findByIdAndUserId(UUID id, UUID userId);

    PageResult<Purchase> findByUserId(UUID userId, PageRequest pageRequest);

    Purchase update(Purchase purchase);

    void delete(Purchase purchase);

}
