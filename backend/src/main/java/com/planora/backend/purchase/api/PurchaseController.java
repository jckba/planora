package com.planora.backend.purchase.api;

import com.planora.backend.common.api.PageResponse;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.purchase.api.dto.request.AddPurchaseItemRequest;
import com.planora.backend.purchase.api.dto.request.AddPurchasePaymentRequest;
import com.planora.backend.purchase.api.dto.request.CreatePurchaseRequest;
import com.planora.backend.purchase.api.dto.request.UpdatePurchaseRequest;
import com.planora.backend.purchase.api.dto.response.PurchaseItemResponse;
import com.planora.backend.purchase.api.dto.response.PurchasePaymentResponse;
import com.planora.backend.purchase.api.dto.response.PurchaseResponse;
import com.planora.backend.purchase.application.cancel.CancelPurchaseUseCase;
import com.planora.backend.purchase.application.complete.CompletePurchaseUseCase;
import com.planora.backend.purchase.application.create.CreatePurchaseCommand;
import com.planora.backend.purchase.application.create.CreatePurchaseUseCase;
import com.planora.backend.purchase.application.delete.DeletePurchaseUseCase;
import com.planora.backend.purchase.application.get.GetPurchaseByIdUseCase;
import com.planora.backend.purchase.application.get.GetPurchasesUseCase;
import com.planora.backend.purchase.application.item.add.AddPurchaseItemCommand;
import com.planora.backend.purchase.application.item.add.AddPurchaseItemUseCase;
import com.planora.backend.purchase.application.item.remove.RemovePurchaseItemCommand;
import com.planora.backend.purchase.application.item.remove.RemovePurchaseItemUseCase;
import com.planora.backend.purchase.application.payment.add.AddPurchasePaymentCommand;
import com.planora.backend.purchase.application.payment.add.AddPurchasePaymentUseCase;
import com.planora.backend.purchase.application.payment.remove.RemovePurchasePaymentCommand;
import com.planora.backend.purchase.application.payment.remove.RemovePurchasePaymentUseCase;
import com.planora.backend.purchase.application.update.UpdatePurchaseCommand;
import com.planora.backend.purchase.application.update.UpdatePurchaseUseCase;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.domain.PurchaseItem;
import com.planora.backend.purchase.domain.PurchasePayment;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final GetPurchasesUseCase getPurchasesUseCase;
    private final GetPurchaseByIdUseCase getPurchaseByIdUseCase;
    private final CreatePurchaseUseCase createPurchaseUseCase;
    private final UpdatePurchaseUseCase updatePurchaseUseCase;
    private final DeletePurchaseUseCase deletePurchaseUseCase;

    private final AddPurchaseItemUseCase addPurchaseItemUseCase;
    private final RemovePurchaseItemUseCase removePurchaseItemUseCase;

    private final AddPurchasePaymentUseCase addPurchasePaymentUseCase;
    private final RemovePurchasePaymentUseCase removePurchasePaymentUseCase;

    private final CompletePurchaseUseCase completePurchaseUseCase;
    private final CancelPurchaseUseCase cancelPurchaseUseCase;

    public PurchaseController(GetPurchasesUseCase getPurchasesUseCase, GetPurchaseByIdUseCase getPurchaseByIdUseCase, CreatePurchaseUseCase createPurchaseUseCase, UpdatePurchaseUseCase updatePurchaseUseCase, DeletePurchaseUseCase deletePurchaseUseCase, AddPurchaseItemUseCase addPurchaseItemUseCase, RemovePurchaseItemUseCase removePurchaseItemUseCase, AddPurchasePaymentUseCase addPurchasePaymentUseCase, RemovePurchasePaymentUseCase removePurchasePaymentUseCase, CompletePurchaseUseCase completePurchaseUseCase, CancelPurchaseUseCase cancelPurchaseUseCase) {
        this.getPurchasesUseCase = getPurchasesUseCase;
        this.getPurchaseByIdUseCase = getPurchaseByIdUseCase;
        this.createPurchaseUseCase = createPurchaseUseCase;
        this.updatePurchaseUseCase = updatePurchaseUseCase;
        this.deletePurchaseUseCase = deletePurchaseUseCase;
        this.addPurchaseItemUseCase = addPurchaseItemUseCase;
        this.removePurchaseItemUseCase = removePurchaseItemUseCase;
        this.addPurchasePaymentUseCase = addPurchasePaymentUseCase;
        this.removePurchasePaymentUseCase = removePurchasePaymentUseCase;
        this.completePurchaseUseCase = completePurchaseUseCase;
        this.cancelPurchaseUseCase = cancelPurchaseUseCase;
    }

    @GetMapping
    public PageResponse<PurchaseResponse> getPurchases(
        @RequestParam UUID userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
        ) {
        PageRequest pageRequest = new PageRequest(page, size);

        PageResult<Purchase> result = getPurchasesUseCase.execute(userId, pageRequest);

        List<PurchaseResponse> content = result.content().stream().map(this::toResponse).toList();

        return new PageResponse<>(content, result.page(), result.size(), result.totalElements());
    }

    @GetMapping("/{purchaseId}")
    public PurchaseResponse getPurchaseById(
        @RequestParam UUID userId,
        @PathVariable UUID purchaseId
    ) {
        Purchase purchase = getPurchaseByIdUseCase.execute(userId, purchaseId);
        return toResponse(purchase);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResponse createPurchase(
        @RequestParam UUID userId,
        @Valid @RequestBody CreatePurchaseRequest request
    ) {
        CreatePurchaseCommand command = new CreatePurchaseCommand(
            request.expectedDate(),
            request.notes()
        );
        Purchase purchase = createPurchaseUseCase.execute(userId, command);
        return toResponse(purchase);
    }

    @PutMapping("/{purchaseId}")
    public PurchaseResponse updatePurchase(
        @RequestParam UUID userId,
        @PathVariable UUID purchaseId,
        @Valid @RequestBody UpdatePurchaseRequest request
    ) {
        UpdatePurchaseCommand command = new UpdatePurchaseCommand(
            purchaseId,
            userId,
            request.expectedDate(),
            request.notes()
        );
        Purchase purchase = updatePurchaseUseCase.execute(command);
        return toResponse(purchase);
    }

    @DeleteMapping("/{purchaseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePurchase(
        @RequestParam UUID userId,
        @PathVariable UUID purchaseId
    ) {
        deletePurchaseUseCase.execute(userId, purchaseId);
    }

    @PostMapping("/{purchaseId}/items")
    public PurchaseResponse addItem(
        @RequestParam UUID userId,
        @PathVariable UUID purchaseId,
        @Valid @RequestBody AddPurchaseItemRequest request
    ) {
        AddPurchaseItemCommand command = new AddPurchaseItemCommand(
            purchaseId,
            userId,
            request.categoryId(),
            request.name(),
            request.quantity(),
            request.unitPrice()
        );
        Purchase purchase = addPurchaseItemUseCase.execute(command);

        return toResponse(purchase);
    }

    @DeleteMapping("/{purchaseId}/items/{itemId}")
    public PurchaseResponse removeItem(
        @RequestParam UUID userId,
        @PathVariable UUID purchaseId,
        @PathVariable UUID itemId
    ) {
        RemovePurchaseItemCommand command = new RemovePurchaseItemCommand(
            purchaseId,
            userId,
            itemId
        );
        Purchase purchase = removePurchaseItemUseCase.execute(command);
        return toResponse(purchase);
    }

    @PostMapping("/{purchaseId}/payments")
    public PurchaseResponse addPayment(
        @RequestParam UUID userId,
        @PathVariable UUID purchaseId,
        @Valid @RequestBody AddPurchasePaymentRequest request
    ) {
        AddPurchasePaymentCommand command = new AddPurchasePaymentCommand(
            purchaseId,
            userId,
            request.accountId(),
            request.amount()
        );
        Purchase purchase = addPurchasePaymentUseCase.execute(command);
        return toResponse(purchase);
    }

    @DeleteMapping("/{purchaseId}/payments/{paymentId}")
    public PurchaseResponse removePayment(
        @RequestParam UUID userId,
        @PathVariable UUID purchaseId,
        @PathVariable UUID paymentId
    ) {
        RemovePurchasePaymentCommand command = new RemovePurchasePaymentCommand(
            purchaseId,
            userId,
            paymentId
        );
        Purchase purchase = removePurchasePaymentUseCase.execute(command);
        return toResponse(purchase);
    }

    @PostMapping("/{purchaseId}/complete")
    public PurchaseResponse completePurchase(
        @RequestParam UUID userId,
        @PathVariable UUID purchaseId
    ) {
        Purchase purchase = completePurchaseUseCase.execute(userId, purchaseId);
        return toResponse(purchase);
    }

    @PostMapping("/{purchaseId}/cancel")
    public PurchaseResponse cancelPurchase(
        @RequestParam UUID userId,
        @PathVariable UUID purchaseId
    ) {
        Purchase purchase = cancelPurchaseUseCase.execute(userId, purchaseId);
        return toResponse(purchase);
    }

    private PurchaseResponse toResponse(Purchase purchase) {

        List<PurchaseItemResponse> items = purchase.getItems().stream().map(this::toItemResponse).toList();

        List<PurchasePaymentResponse> payments = purchase.getPayments().stream().map(this::toPaymentResponse).toList();

        return new PurchaseResponse(
            purchase.getId(),
            purchase.getStatus(),
            purchase.getExpectedDate(),
            purchase.getPurchaseDate(),
            purchase.getTotal(),
            purchase.getNotes(),
            purchase.getCreatedAt(),
            purchase.getUpdatedAt(),
            purchase.getVersion(),
            items,
            payments
        );
    }

    private PurchaseItemResponse toItemResponse(PurchaseItem item) {
        return new PurchaseItemResponse(
            item.getId(),
            item.getCategoryId(),
            item.getName(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.subtotal()
        );
    }

    private PurchasePaymentResponse toPaymentResponse(PurchasePayment payment) {
        return new PurchasePaymentResponse(
            payment.getId(),
            payment.getAccountId(),
            payment.getAmount()
        );
    }
}
