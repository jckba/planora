package com.planora.backend.purchase.application.payment.add;

import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.domain.PurchasePayment;
import com.planora.backend.purchase.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

@Service
public class AddPurchasePaymentService implements AddPurchasePaymentUseCase{

    private final PurchaseRepository purchaseRepository;

    public AddPurchasePaymentService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @Override
    public Purchase execute(AddPurchasePaymentCommand command) {
        Purchase purchase = purchaseRepository.findByIdAndUserId(
            command.purchaseId(),
            command.userId()
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
