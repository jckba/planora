package com.planora.backend.purchase.application.payment.add;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.common.security.CurrentUser;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.domain.PurchasePayment;
import com.planora.backend.purchase.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

@Service
public class AddPurchasePaymentService implements AddPurchasePaymentUseCase{

    private final PurchaseRepository purchaseRepository;
    private final CurrentUser currentUser;

    public AddPurchasePaymentService(PurchaseRepository purchaseRepository, CurrentUser currentUser) {
        this.purchaseRepository = purchaseRepository;
        this.currentUser = currentUser;
    }

    @Override
    public Purchase execute(AddPurchasePaymentCommand command) {
        Purchase purchase = purchaseRepository.findByIdAndUserId(
            command.purchaseId(),
            currentUser.userId()
        ).orElseThrow(
            () -> new ResourceNotFoundException("Purchase not found")
        );

        PurchasePayment payment = PurchasePayment.create(
            command.accountId(),
            command.amount()
        );
        purchase.addPayment(payment);
        return purchaseRepository.update(purchase);

    }
}
