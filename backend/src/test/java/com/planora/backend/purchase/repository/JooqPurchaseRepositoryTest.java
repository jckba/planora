package com.planora.backend.purchase.repository;

import com.planora.backend.common.exception.OptimisticLockException;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.domain.PurchaseItem;
import com.planora.backend.purchase.domain.PurchasePayment;
import com.planora.backend.purchase.domain.PurchaseStatus;
import com.planora.persistence.jooq.tables.records.PurchaseRecord;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static com.planora.persistence.jooq.tables.Account.ACCOUNT;
import static com.planora.persistence.jooq.tables.AppUser.APP_USER;
import static com.planora.persistence.jooq.tables.Category.CATEGORY;
import static com.planora.persistence.jooq.tables.Purchase.PURCHASE;
import static com.planora.persistence.jooq.tables.PurchaseItem.PURCHASE_ITEM;
import static com.planora.persistence.jooq.tables.PurchasePayment.PURCHASE_PAYMENT;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JooqPurchaseRepositoryTest {

    private final PurchaseRepository purchaseRepository;
    private final DSLContext dsl;

    @Autowired
    JooqPurchaseRepositoryTest(PurchaseRepository purchaseRepository, DSLContext dsl) {
        this.purchaseRepository = purchaseRepository;
        this.dsl = dsl;
    }

    private UUID createUserForTest() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        OffsetDateTime nowOffset = now.atOffset(ZoneOffset.UTC);

        dsl.insertInto(APP_USER)
            .set(APP_USER.ID, userId)
            .set(APP_USER.USERNAME, "testuser-" + userId)
            .set(APP_USER.EMAIL, "test-" + userId + "@planora.test")
            .set(APP_USER.PASSWORD_HASH, "test-password")
            .set(APP_USER.FIRST_NAME, "Test")
            .set(APP_USER.LAST_NAME, "User")
            .set(APP_USER.PRIMARY_CURRENCY_ID, (short) 1)
            .set(APP_USER.PASSWORD_UPDATED_AT, nowOffset)
            .set(APP_USER.CREATED_AT, nowOffset)
            .set(APP_USER.UPDATED_AT, nowOffset)
            .set(APP_USER.VERSION, 0)
            .execute();
        return userId;
    }

    private UUID createAccountForTest(UUID userId) {
        UUID accountId = UUID.randomUUID();

        Instant now = Instant.now();
        OffsetDateTime nowOffset = now.atOffset(ZoneOffset.UTC);

        dsl.insertInto(ACCOUNT)
            .set(ACCOUNT.ID, accountId)
            .set(ACCOUNT.USER_ID, userId)
            .set(ACCOUNT.ACCOUNT_TYPE_ID, (short) 1)
            .set(ACCOUNT.CURRENCY_ID, (short) 1)
            .set(ACCOUNT.NAME, "Test Account")
            .set(ACCOUNT.BALANCE, BigDecimal.ZERO)
            .set(ACCOUNT.CREATED_AT, nowOffset)
            .set(ACCOUNT.UPDATED_AT, nowOffset)
            .set(ACCOUNT.VERSION, 0)
            .execute();
        return accountId;
    }

    private UUID createCategoryForTest(UUID userId) {
        UUID categoryId = UUID.randomUUID();

        Instant now = Instant.now();
        OffsetDateTime nowOffset = now.atOffset(ZoneOffset.UTC);

        dsl.insertInto(CATEGORY)
            .set(CATEGORY.ID, categoryId)
            .set(CATEGORY.USER_ID, userId)
            .set(CATEGORY.NAME, "Food")
            .set(CATEGORY.COLOR, "#FF0000")
            .set(CATEGORY.ICON, "restaurant")
            .set(CATEGORY.CREATED_AT, nowOffset)
            .set(CATEGORY.UPDATED_AT, nowOffset)
            .set(CATEGORY.DELETED_AT, (OffsetDateTime) null)
            .set(CATEGORY.VERSION, 0)
            .execute();
        return categoryId;
    }

    @Test
    @Transactional
    void shouldSavePurchaseWithItemsAndPayments() {

        UUID userId = createUserForTest();

        UUID categoryId =
            createCategoryForTest(userId);

        UUID accountId =
            createAccountForTest(userId);

        Purchase purchase =
            Purchase.create(
                userId,
                Instant.parse(
                    "2026-09-01T12:00:00Z"
                ),
                "Birthday purchase"
            );

        purchase.addItem(
            PurchaseItem.create(
                categoryId,
                "Laptop",
                BigDecimal.ONE,
                new BigDecimal("1500.00")
            )
        );

        purchase.addPayment(
            PurchasePayment.create(
                accountId,
                new BigDecimal("1500.00")
            )
        );

        purchaseRepository.save(purchase);

        PurchaseRecord purchaseRecord =
            dsl.selectFrom(PURCHASE)
                .where(
                    PURCHASE.ID.eq(
                        purchase.getId()
                    )
                )
                .fetchOne();

        assertNotNull(purchaseRecord);

        assertEquals(
            purchase.getUserId(),
            purchaseRecord.getUserId()
        );

        assertEquals(
            "PENDING",
            purchaseRecord.getStatus()
        );

        assertEquals(
            0,
            new BigDecimal("1500.00")
                .compareTo(
                    purchaseRecord.getTotal()
                )
        );

        assertEquals(
            1,
            dsl.selectCount()
                .from(PURCHASE_ITEM)
                .where(
                    PURCHASE_ITEM.PURCHASE_ID.eq(
                        purchase.getId()
                    )
                )
                .fetchOne(0, Integer.class)
        );

        assertEquals(
            1,
            dsl.selectCount()
                .from(PURCHASE_PAYMENT)
                .where(
                    PURCHASE_PAYMENT.PURCHASE_ID.eq(
                        purchase.getId()
                    )
                )
                .fetchOne(0, Integer.class)
        );
    }

    @Test
    @Transactional
    void shouldFindPurchaseWithItemsAndPayments() {

        UUID userId = createUserForTest();

        UUID categoryId =
            createCategoryForTest(userId);

        UUID accountId =
            createAccountForTest(userId);

        Purchase purchase =
            Purchase.create(
                userId,
                Instant.parse(
                    "2026-09-01T12:00:00Z"
                ),
                "Birthday purchase"
            );

        PurchaseItem item =
            PurchaseItem.create(
                categoryId,
                "Laptop",
                BigDecimal.ONE,
                new BigDecimal("1500.00")
            );

        PurchasePayment payment =
            PurchasePayment.create(
                accountId,
                new BigDecimal("1500.00")
            );

        purchase.addItem(item);
        purchase.addPayment(payment);

        purchaseRepository.save(purchase);

        Optional<Purchase> result =
            purchaseRepository.findById(
                purchase.getId()
            );

        assertTrue(result.isPresent());

        Purchase found =
            result.orElseThrow();

        assertEquals(
            purchase.getId(),
            found.getId()
        );

        assertEquals(
            purchase.getUserId(),
            found.getUserId()
        );

        assertEquals(
            PurchaseStatus.PENDING,
            found.getStatus()
        );

        assertEquals(
            1,
            found.getItems().size()
        );

        assertEquals(
            1,
            found.getPayments().size()
        );

        assertEquals(
            item.getId(),
            found.getItems().getFirst().getId()
        );

        assertEquals(
            payment.getId(),
            found.getPayments().getFirst().getId()
        );

        assertEquals(
            0,
            purchase.getTotal()
                .compareTo(found.getTotal())
        );
    }

    @Test
    @Transactional
    void shouldNotFindPurchaseForAnotherUser() {

        UUID ownerId = createUserForTest();
        UUID anotherUserId = createUserForTest();

        Purchase purchase =
            Purchase.create(
                ownerId,
                null,
                null
            );

        purchaseRepository.save(purchase);

        Optional<Purchase> result =
            purchaseRepository.findByIdAndUserId(
                purchase.getId(),
                anotherUserId
            );

        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    void shouldThrowOptimisticLockExceptionWhenVersionIsStale() {
        UUID userId = createUserForTest();

        Purchase purchase =
            Purchase.create(
                userId,
                null,
                null
            );

        purchaseRepository.save(purchase);

        Purchase loaded =
            purchaseRepository
                .findByIdAndUserId(
                    purchase.getId(),
                    userId
                )
                .orElseThrow();

        dsl.update(PURCHASE)
            .set(
                PURCHASE.VERSION,
                loaded.getVersion() + 1
            )
            .where(
                PURCHASE.ID.eq(
                    loaded.getId()
                )
            )
            .execute();

        assertThrows(
            OptimisticLockException.class,
            () -> purchaseRepository.update(loaded)
        );
    }

    @Test
    @Transactional
    void shouldDeletePurchaseWithItemsAndPayments() {
        UUID userId = createUserForTest();
        UUID categoryId = createCategoryForTest(userId);
        UUID accountId = createAccountForTest(userId);

        Purchase purchase =
            Purchase.create(
                userId,
                null,
                null
            );

        purchase.addItem(
            PurchaseItem.create(
                categoryId,
                "Laptop",
                BigDecimal.ONE,
                new BigDecimal("1500.00")
            )
        );

        purchase.addPayment(
            PurchasePayment.create(
                accountId,
                new BigDecimal("1500.00")
            )
        );

        purchaseRepository.save(purchase);

        purchaseRepository.delete(purchase);

        assertFalse(dsl.fetchExists(
            dsl.selectFrom(PURCHASE)
                .where(
                    PURCHASE.ID.eq(
                        purchase.getId()
                    )
                )
        ));

        assertEquals(
            0,
            dsl.selectCount()
                .from(PURCHASE_ITEM)
                .where(
                    PURCHASE_ITEM.PURCHASE_ID.eq(
                        purchase.getId()
                    )
                )
                .fetchOne(0, Integer.class)
        );

        assertEquals(
            0,
            dsl.selectCount()
                .from(PURCHASE_PAYMENT)
                .where(
                    PURCHASE_PAYMENT.PURCHASE_ID.eq(
                        purchase.getId()
                    )
                )
                .fetchOne(0, Integer.class)
        );
    }

    @Test
    @Transactional
    void shouldRejectUpdateWhenVersionIsStale() {
        UUID userId = createUserForTest();

        Purchase purchase = Purchase.create(
            userId,
            Instant.parse("2026-09-15T12:00:00Z"),
            "Original notes"
        );

        purchaseRepository.save(purchase);

        dsl.update(PURCHASE)
            .set(PURCHASE.VERSION, purchase.getVersion() + 1)
            .where(PURCHASE.ID.eq(purchase.getId()))
            .execute();

        assertThrows(
            OptimisticLockException.class,
            () -> purchaseRepository.update(purchase)
        );
    }

    @Test
    @Transactional
    void shouldRejectDeleteWhenVersionIsStale() {
        UUID userId = createUserForTest();

        Purchase purchase = Purchase.create(
            userId,
            Instant.parse("2026-09-15T12:00:00Z"),
            "Purchase"
        );

        purchaseRepository.save(purchase);

        dsl.update(PURCHASE)
            .set(PURCHASE.VERSION, purchase.getVersion() + 1)
            .where(PURCHASE.ID.eq(purchase.getId()))
            .execute();

        assertThrows(
            OptimisticLockException.class,
            () -> purchaseRepository.delete(purchase)
        );
    }

    @Test
    @Transactional
    void shouldNotFindPurchaseBelongingToAnotherUser() {
        UUID ownerUserId = createUserForTest();
        UUID otherUserId = createUserForTest();

        Purchase purchase = Purchase.create(
            ownerUserId,
            Instant.parse("2026-09-15T12:00:00Z"),
            "Private purchase"
        );

        purchaseRepository.save(purchase);

        Optional<Purchase> result =
            purchaseRepository.findByIdAndUserId(
                purchase.getId(),
                otherUserId
            );

        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    void shouldDeletePurchase() {
        UUID userId = createUserForTest();

        Purchase purchase = Purchase.create(
            userId,
            Instant.parse("2026-09-15T12:00:00Z"),
            "Purchase to delete"
        );

        purchaseRepository.save(purchase);

        purchaseRepository.delete(purchase);

        Optional<Purchase> result =
            purchaseRepository.findById(purchase.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    void shouldDeletePurchaseItemsAndPaymentsWithPurchase() {
        UUID userId = createUserForTest();
        UUID accountId = createAccountForTest(userId);
        UUID categoryId = createCategoryForTest(userId);

        Purchase purchase = Purchase.create(
            userId,
            Instant.parse("2026-09-15T12:00:00Z"),
            "Purchase"
        );

        purchase.addItem(PurchaseItem.create(
            categoryId,
            "Product",
            new BigDecimal("2"),
            new BigDecimal("10.00")
        ));

        purchase.addPayment(PurchasePayment.create(
            accountId,
            new BigDecimal("20.00")
        ));

        purchaseRepository.save(purchase);

        UUID purchaseId = purchase.getId();

        purchaseRepository.delete(purchase);

        assertEquals(
            0,
            dsl.fetchCount(
                PURCHASE_ITEM,
                PURCHASE_ITEM.PURCHASE_ID.eq(purchaseId)
            )
        );

        assertEquals(
            0,
            dsl.fetchCount(
                PURCHASE_PAYMENT,
                PURCHASE_PAYMENT.PURCHASE_ID.eq(purchaseId)
            )
        );
    }





}
