package com.planora.backend.purchase.application.payment.remove;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.common.security.CurrentUser;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

@Service
public class RemovePurchasePaymentService implements RemovePurchasePaymentUseCase{

    private final PurchaseRepository purchaseRepository;
    private final CurrentUser currentUser;

    public RemovePurchasePaymentService(PurchaseRepository purchaseRepository, CurrentUser currentUser) {
        this.purchaseRepository = purchaseRepository;
        this.currentUser = currentUser;
    }

    @Override
    public Purchase execute(RemovePurchasePaymentCommand command) {
        Purchase purchase = purchaseRepository.findByIdAndUserId(
            command.purchaseId(),
            currentUser.userId()
        ).orElseThrow(
            () -> new ResourceNotFoundException("Purchase not found")
        );

        purchase.removePayment(command.paymentId());
        return purchaseRepository.update(purchase);
    }
}
