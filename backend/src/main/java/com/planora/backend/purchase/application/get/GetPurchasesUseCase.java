package com.planora.backend.purchase.application.get;

import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.purchase.domain.Purchase;

import java.util.UUID;

public interface GetPurchasesUseCase {
    PageResult<Purchase> execute(UUID userId, PageRequest pageRequest);
}
