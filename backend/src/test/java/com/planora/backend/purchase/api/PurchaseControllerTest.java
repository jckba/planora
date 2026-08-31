package com.planora.backend.purchase.api;

import com.planora.backend.common.exception.GlobalExceptionHandler;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
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
import com.planora.backend.purchase.domain.PurchaseStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurchaseController.class)
@Import(GlobalExceptionHandler.class)
class PurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetPurchasesUseCase getPurchasesUseCase;
    @MockitoBean
    private GetPurchaseByIdUseCase getPurchaseByIdUseCase;
    @MockitoBean
    private CreatePurchaseUseCase createPurchaseUseCase;
    @MockitoBean
    private UpdatePurchaseUseCase updatePurchaseUseCase;
    @MockitoBean
    private DeletePurchaseUseCase deletePurchaseUseCase;

    @MockitoBean
    private AddPurchaseItemUseCase addPurchaseItemUseCase;
    @MockitoBean
    private RemovePurchaseItemUseCase removePurchaseItemUseCase;

    @MockitoBean
    private AddPurchasePaymentUseCase addPurchasePaymentUseCase;
    @MockitoBean
    private RemovePurchasePaymentUseCase removePurchasePaymentUseCase;

    @MockitoBean
    private CompletePurchaseUseCase completePurchaseUseCase;
    @MockitoBean
    private CancelPurchaseUseCase cancelPurchaseUseCase;

    //GET    /api/purchases
    @Test
    void shouldGetPurchases() throws Exception {
        UUID userId = UUID.randomUUID();

        Purchase purchase = Purchase.create(
            userId,
            Instant.parse("2026-08-10T12:00:00Z"),
            "Notes"
        );
        PageRequest pageRequest = new PageRequest(0, 10);

        PageResult<Purchase> pageResult = new PageResult<>(
            List.of(purchase),
            0,
            10,
            1
        );

        when(
            getPurchasesUseCase.execute(
                userId,
                pageRequest
            )
        ).thenReturn(pageResult);

        mockMvc.perform(
                get("/api/purchases")
                    .param("userId", userId.toString())
                    .param("page", "0")
                    .param("size", "10")
            ).andExpect(status().isOk())
            .andExpect(
                jsonPath("$.content.length()")
                    .value(1))
            .andExpect(
                jsonPath("$.content[0].id")
                    .value(purchase.getId().toString())
            )
            .andExpect(
                jsonPath("$.page")
                    .value(0)
            )
            .andExpect(
                jsonPath("$.size")
                    .value(10)
            )
            .andExpect(
                jsonPath("$.totalElements")
                    .value(1)
            );

        verify(getPurchasesUseCase)
            .execute(userId, pageRequest);
    }

    //GET /api/{purchaseId}
    @Test
    void shouldGetPurchaseById() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();

        Purchase purchase = Purchase.create(
            userId,
            Instant.parse("2026-08-10T12:00:00Z"),
            "Notes"
        );

        when(
            getPurchaseByIdUseCase.execute(
                userId,
                purchaseId
            )
        ).thenReturn(purchase);

        mockMvc.perform(
                get("/api/purchases/{purchaseId}", purchaseId)
                    .param("userId", userId.toString())
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.id")
                    .value(purchase.getId().toString())
            )
            .andExpect(
                jsonPath("$.status")
                    .value("PENDING")
            )
            .andExpect(
                jsonPath("$.expectedDate")
                    .value("2026-08-10T12:00:00Z")
            )
            .andExpect(
                jsonPath("$.notes")
                    .value("Notes")
            )
            .andExpect(
                jsonPath("$.total")
                    .value(0)
            );

        verify(getPurchaseByIdUseCase)
            .execute(
                userId,
                purchaseId
            );
    }

    //POST   /api/purchases
    @Test
    void shouldCreatePurchase() throws Exception {
        UUID userId = UUID.randomUUID();

        Instant expectedDate =
            Instant.parse("2026-08-10T12:00:00Z");

        Purchase purchase =
            Purchase.create(
                userId,
                expectedDate,
                "Notes"
            );

        CreatePurchaseCommand command =
            new CreatePurchaseCommand(
                expectedDate,
                "Notes"
            );

        when(
            createPurchaseUseCase.execute(
                userId,
                command
            )
        ).thenReturn(purchase);

        mockMvc.perform(
                post("/api/purchases")
                    .param(
                        "userId",
                        userId.toString()
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "expectedDate": "2026-08-10T12:00:00Z",
                            "notes": "Notes"
                        }
                        """)
            )
            .andExpect(status().isCreated())
            .andExpect(
                jsonPath("$.id")
                    .value(purchase.getId().toString())
            )
            .andExpect(
                jsonPath("$.status")
                    .value("PENDING")
            )
            .andExpect(
                jsonPath("$.expectedDate")
                    .value("2026-08-10T12:00:00Z")
            )
            .andExpect(
                jsonPath("$.notes")
                    .value("Notes")
            )
            .andExpect(
                jsonPath("$.total")
                    .value(0)
            );

        verify(
            createPurchaseUseCase
        ).execute(
            userId,
            command
        );
    }

    //PUT    /api/purchases/{purchaseId}
    @Test
    void shouldUpdatePurchase() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();

        Instant expectedDate =
            Instant.parse("2026-08-10T12:00:00Z");

        Purchase purchase = Purchase.restore(
            purchaseId,
            userId,
            PurchaseStatus.PENDING,
            expectedDate,
            null,
            BigDecimal.ZERO,
            "Notes",
            Instant.parse("2026-08-01T12:00:00Z"),
            Instant.parse("2026-08-01T12:00:00Z"),
            0,
            List.of(),
            List.of()
        );

        UpdatePurchaseCommand command = new UpdatePurchaseCommand(
            purchaseId,
            userId,
            expectedDate,
            "Notes"
        );
        when(
            updatePurchaseUseCase.execute(command)
        ).thenReturn(purchase);

        mockMvc.perform(
                put("/api/purchases/{purchaseId}", purchaseId)
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                                "expectedDate": "2026-08-10T12:00:00Z",
                                "notes": "Notes"
                            }
                        """)
            ).andExpect(status().isOk())
            .andExpect(
                jsonPath("$.id")
                    .value(purchase.getId().toString())
            )
            .andExpect(
                jsonPath("$.status")
                    .value("PENDING")
            )
            .andExpect(
                jsonPath("$.expectedDate")
                    .value("2026-08-10T12:00:00Z")
            )
            .andExpect(
                jsonPath("$.notes")
                    .value("Notes")
            )
            .andExpect(
                jsonPath("$.total")
                    .value(0)
            );

        verify(
            updatePurchaseUseCase
        ).execute(command);
    }

    //DELETE /api/purchases/{purchaseId}
    @Test
    void shouldDeletePurchase() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();

        mockMvc.perform(
                delete("/api/purchases/{purchaseId}", purchaseId)
                    .param("userId", userId.toString())
            ).andExpect(status().isNoContent())
            .andExpect(content().string(""));

        verify(deletePurchaseUseCase).execute(userId, purchaseId);
    }

    //POST   /api/purchases/{purchaseId}/complete
    @Test
    void shouldCompletePurchase() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        Purchase purchase =
            Purchase.create(
                userId,
                null,
                "Notes"
            );

        PurchaseItem item =
            PurchaseItem.create(
                categoryId,
                "Keyboard",
                BigDecimal.ONE,
                new BigDecimal("100.00")
            );

        PurchasePayment payment =
            PurchasePayment.create(
                accountId,
                new BigDecimal("100.00")
            );

        purchase.addItem(item);
        purchase.addPayment(payment);

        purchase.complete();

        when(
            completePurchaseUseCase.execute(
                userId,
                purchaseId
            )
        ).thenReturn(purchase);

        mockMvc.perform(
                post(
                    "/api/purchases/{purchaseId}/complete",
                    purchaseId
                )
                    .param("userId", userId.toString())
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.status")
                    .value("COMPLETED")
            )
            .andExpect(
                jsonPath("$.total")
                    .value(100.00)
            );

        verify(completePurchaseUseCase)
            .execute(userId, purchaseId);
    }

    // POST /api/purchases/{purchaseId}/cancel
    @Test
    void shouldCancelPurchase() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();

        Purchase purchase =
            Purchase.create(
                userId,
                null,
                "Notes"
            );

        purchase.cancel();

        when(
            cancelPurchaseUseCase.execute(
                userId,
                purchaseId
            )
        ).thenReturn(purchase);

        mockMvc.perform(
                post(
                    "/api/purchases/{purchaseId}/cancel",
                    purchaseId
                )
                    .param("userId", userId.toString())
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.status")
                    .value("CANCELLED")
            );

        verify(cancelPurchaseUseCase)
            .execute(userId, purchaseId);
    }

    // POST /api/purchases/{purchaseId}/items
    @Test
    void shouldAddItem() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Purchase purchase =
            Purchase.create(
                userId,
                null,
                "Notes"
            );

        AddPurchaseItemCommand command =
            new AddPurchaseItemCommand(
                purchaseId,
                userId,
                categoryId,
                "Milk",
                new BigDecimal("2"),
                new BigDecimal("5.50")
            );

        when(
            addPurchaseItemUseCase.execute(command)
        ).thenReturn(purchase);

        mockMvc.perform(
                post(
                    "/api/purchases/{purchaseId}/items",
                    purchaseId
                )
                    .param(
                        "userId",
                        userId.toString()
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                {
                    "categoryId": "%s",
                    "name": "Milk",
                    "quantity": 2,
                    "unitPrice": 5.50
                }
                """.formatted(categoryId))
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.id")
                    .value(purchase.getId().toString())
            );

        verify(addPurchaseItemUseCase)
            .execute(command);
    }

    // DELETE /api/purchases/{purchaseId}/items/{itemId}
    @Test
    void shouldRemoveItem() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Purchase purchase =
            Purchase.create(
                userId,
                null,
                "Notes"
            );

        RemovePurchaseItemCommand command =
            new RemovePurchaseItemCommand(
                purchaseId,
                userId,
                itemId
            );

        when(
            removePurchaseItemUseCase.execute(command)
        ).thenReturn(purchase);

        mockMvc.perform(
                delete(
                    "/api/purchases/{purchaseId}/items/{itemId}",
                    purchaseId,
                    itemId
                )
                    .param(
                        "userId",
                        userId.toString()
                    )
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.id")
                    .value(purchase.getId().toString())
            );

        verify(removePurchaseItemUseCase)
            .execute(command);
    }

    // POST /api/purchases/{purchaseId}/payments
    @Test
    void shouldAddPayment() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        Purchase purchase =
            Purchase.create(
                userId,
                null,
                "Notes"
            );

        AddPurchasePaymentCommand command =
            new AddPurchasePaymentCommand(
                purchaseId,
                userId,
                accountId,
                new BigDecimal("25.00")
            );

        when(
            addPurchasePaymentUseCase.execute(command)
        ).thenReturn(purchase);

        mockMvc.perform(
                post(
                    "/api/purchases/{purchaseId}/payments",
                    purchaseId
                )
                    .param(
                        "userId",
                        userId.toString()
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                {
                    "accountId": "%s",
                    "amount": 25.00
                }
                """.formatted(accountId))
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.id")
                    .value(purchase.getId().toString())
            );

        verify(addPurchasePaymentUseCase)
            .execute(command);
    }

    // DELETE /api/purchases/{purchaseId}/payments/{paymentId}
    @Test
    void shouldRemovePayment() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Purchase purchase =
            Purchase.create(
                userId,
                null,
                "Notes"
            );

        RemovePurchasePaymentCommand command =
            new RemovePurchasePaymentCommand(
                purchaseId,
                userId,
                paymentId
            );

        when(
            removePurchasePaymentUseCase.execute(command)
        ).thenReturn(purchase);

        mockMvc.perform(
                delete(
                    "/api/purchases/{purchaseId}/payments/{paymentId}",
                    purchaseId,
                    paymentId
                )
                    .param(
                        "userId",
                        userId.toString()
                    )
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.id")
                    .value(purchase.getId().toString())
            );

        verify(removePurchasePaymentUseCase)
            .execute(command);
    }

    @Test
    void shouldAddPurchaseItem() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Purchase purchase =
            Purchase.create(
                userId,
                Instant.parse("2026-08-10T12:00:00Z"),
                "Notes"
            );

        PurchaseItem item =
            PurchaseItem.create(
                categoryId,
                "Keyboard",
                new BigDecimal("2"),
                new BigDecimal("25.50")
            );

        purchase.addItem(item);

        AddPurchaseItemCommand command =
            new AddPurchaseItemCommand(
                purchaseId,
                userId,
                categoryId,
                "Keyboard",
                new BigDecimal("2"),
                new BigDecimal("25.50")
            );

        when(
            addPurchaseItemUseCase.execute(command)
        ).thenReturn(purchase);

        mockMvc.perform(
                post(
                    "/api/purchases/{purchaseId}/items",
                    purchaseId
                )
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                {
                    "categoryId": "%s",
                    "name": "Keyboard",
                    "quantity": 2,
                    "unitPrice": 25.50
                }
                """.formatted(categoryId))
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.id")
                    .value(purchase.getId().toString())
            )
            .andExpect(
                jsonPath("$.items.length()")
                    .value(1)
            )
            .andExpect(
                jsonPath("$.items[0].categoryId")
                    .value(categoryId.toString())
            )
            .andExpect(
                jsonPath("$.items[0].name")
                    .value("Keyboard")
            )
            .andExpect(
                jsonPath("$.items[0].quantity")
                    .value(2)
            )
            .andExpect(
                jsonPath("$.items[0].unitPrice")
                    .value(25.50)
            )
            .andExpect(
                jsonPath("$.items[0].subtotal")
                    .value(51.00)
            );

        verify(addPurchaseItemUseCase)
            .execute(command);
    }

    @Test
    void shouldRemovePurchaseItem() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Purchase purchase =
            Purchase.create(
                userId,
                null,
                "Notes"
            );

        RemovePurchaseItemCommand command =
            new RemovePurchaseItemCommand(
                purchaseId,
                userId,
                itemId
            );

        when(
            removePurchaseItemUseCase.execute(command)
        ).thenReturn(purchase);

        mockMvc.perform(
                delete(
                    "/api/purchases/{purchaseId}/items/{itemId}",
                    purchaseId,
                    itemId
                )
                    .param("userId", userId.toString())
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.id")
                    .value(purchase.getId().toString())
            )
            .andExpect(
                jsonPath("$.items.length()")
                    .value(0)
            );

        verify(removePurchaseItemUseCase)
            .execute(command);
    }

    @Test
    void shouldAddPurchasePayment() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        Purchase purchase =
            Purchase.create(
                userId,
                null,
                "Notes"
            );

        PurchasePayment payment =
            PurchasePayment.create(
                accountId,
                new BigDecimal("100.00")
            );

        purchase.addPayment(payment);

        AddPurchasePaymentCommand command =
            new AddPurchasePaymentCommand(
                purchaseId,
                userId,
                accountId,
                new BigDecimal("100.00")
            );

        when(
            addPurchasePaymentUseCase.execute(command)
        ).thenReturn(purchase);

        mockMvc.perform(
                post(
                    "/api/purchases/{purchaseId}/payments",
                    purchaseId
                )
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                {
                    "accountId": "%s",
                    "amount": 100.00
                }
                """.formatted(accountId))
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.payments.length()")
                    .value(1)
            )
            .andExpect(
                jsonPath("$.payments[0].accountId")
                    .value(accountId.toString())
            )
            .andExpect(
                jsonPath("$.payments[0].amount")
                    .value(100.00)
            );

        verify(addPurchasePaymentUseCase)
            .execute(command);
    }

    @Test
    void shouldRemovePurchasePayment() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Purchase purchase =
            Purchase.create(
                userId,
                null,
                "Notes"
            );

        RemovePurchasePaymentCommand command =
            new RemovePurchasePaymentCommand(
                purchaseId,
                userId,
                paymentId
            );

        when(
            removePurchasePaymentUseCase.execute(command)
        ).thenReturn(purchase);

        mockMvc.perform(
                delete(
                    "/api/purchases/{purchaseId}/payments/{paymentId}",
                    purchaseId,
                    paymentId
                )
                    .param("userId", userId.toString())
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.id")
                    .value(purchase.getId().toString())
            )
            .andExpect(
                jsonPath("$.payments.length()")
                    .value(0)
            );

        verify(removePurchasePaymentUseCase)
            .execute(command);
    }




}
