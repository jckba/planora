package com.planora.backend.purchase.repository.jooq;

import com.planora.backend.common.exception.OptimisticLockException;import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import com.planora.backend.purchase.domain.Purchase;
import com.planora.backend.purchase.domain.PurchaseItem;
import com.planora.backend.purchase.domain.PurchasePayment;
import com.planora.backend.purchase.domain.PurchaseStatus;
import com.planora.backend.purchase.repository.PurchaseRepository;
import com.planora.persistence.jooq.tables.records.PurchaseItemRecord;
import com.planora.persistence.jooq.tables.records.PurchasePaymentRecord;
import com.planora.persistence.jooq.tables.records.PurchaseRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.planora.persistence.jooq.tables.Purchase.PURCHASE;
import static com.planora.persistence.jooq.tables.PurchaseItem.PURCHASE_ITEM;
import static com.planora.persistence.jooq.tables.PurchasePayment.PURCHASE_PAYMENT;

@Repository
public class JooqPurchaseRepository implements PurchaseRepository {

    private final DSLContext dsl;

    public JooqPurchaseRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    @Transactional
    public Purchase save(Purchase purchase) {
        dsl.insertInto(PURCHASE)
            .set(PURCHASE.ID, purchase.getId())
            .set(PURCHASE.USER_ID, purchase.getUserId())
            .set(
                PURCHASE.STATUS,
                purchase.getStatus().name()
            )
            .set(
                PURCHASE.EXPECTED_DATE,
                purchase.getExpectedDate() == null
                    ? null
                    : OffsetDateTime.ofInstant(
                    purchase.getExpectedDate(),
                    ZoneOffset.UTC
                )
            )
            .set(
                PURCHASE.PURCHASE_DATE,
                purchase.getPurchaseDate() == null
                    ? null
                    : OffsetDateTime.ofInstant(
                    purchase.getPurchaseDate(),
                    ZoneOffset.UTC
                )
            )
            .set(PURCHASE.TOTAL, purchase.getTotal())
            .set(PURCHASE.NOTES, purchase.getNotes())
            .set(
                PURCHASE.CREATED_AT,
                OffsetDateTime.ofInstant(
                    purchase.getCreatedAt(),
                    ZoneOffset.UTC
                )
            )
            .set(
                PURCHASE.UPDATED_AT,
                OffsetDateTime.ofInstant(
                    purchase.getUpdatedAt(),
                    ZoneOffset.UTC
                )
            )
            .set(PURCHASE.VERSION, purchase.getVersion())
            .execute();

        for (PurchaseItem item : purchase.getItems()) {
            dsl.insertInto(PURCHASE_ITEM)
                .set(PURCHASE_ITEM.ID, item.getId())
                .set(
                    PURCHASE_ITEM.PURCHASE_ID,
                    purchase.getId()
                )
                .set(
                    PURCHASE_ITEM.CATEGORY_ID,
                    item.getCategoryId()
                )
                .set(PURCHASE_ITEM.NAME, item.getName())
                .set(
                    PURCHASE_ITEM.QUANTITY,
                    item.getQuantity()
                )
                .set(
                    PURCHASE_ITEM.UNIT_PRICE,
                    item.getUnitPrice()
                )
                .execute();
        }

        for (PurchasePayment payment :
            purchase.getPayments()) {

            dsl.insertInto(PURCHASE_PAYMENT)
                .set(
                    PURCHASE_PAYMENT.ID,
                    payment.getId()
                )
                .set(
                    PURCHASE_PAYMENT.PURCHASE_ID,
                    purchase.getId()
                )
                .set(
                    PURCHASE_PAYMENT.ACCOUNT_ID,
                    payment.getAccountId()
                )
                .set(
                    PURCHASE_PAYMENT.AMOUNT,
                    payment.getAmount()
                )
                .execute();
        }

        return purchase;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Purchase> findById(UUID id) {
        PurchaseRecord purchaseRecord =
            dsl.selectFrom(PURCHASE)
                .where(PURCHASE.ID.eq(id))
                .fetchOne();

        if (purchaseRecord == null) {
            return Optional.empty();
        }

        List<PurchaseItem> items =
            dsl.selectFrom(PURCHASE_ITEM)
                .where(
                    PURCHASE_ITEM.PURCHASE_ID.eq(id)
                )
                .fetch()
                .map(this::mapToPurchaseItem);

        List<PurchasePayment> payments =
            dsl.selectFrom(PURCHASE_PAYMENT)
                .where(
                    PURCHASE_PAYMENT.PURCHASE_ID.eq(id)
                )
                .fetch()
                .map(this::mapToPurchasePayment);

        return Optional.of(
            mapToPurchase(
                purchaseRecord,
                items,
                payments
            )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Purchase> findByIdAndUserId(UUID id, UUID userId) {
        PurchaseRecord purchaseRecord =
            dsl.selectFrom(PURCHASE)
                .where(
                    PURCHASE.ID.eq(id)
                        .and(PURCHASE.USER_ID.eq(userId))
                )
                .fetchOne();

        if (purchaseRecord == null) {
            return Optional.empty();
        }

        List<PurchaseItem> items =
            dsl.selectFrom(PURCHASE_ITEM)
                .where(
                    PURCHASE_ITEM.PURCHASE_ID.eq(id)
                )
                .fetch()
                .map(this::mapToPurchaseItem);

        List<PurchasePayment> payments =
            dsl.selectFrom(PURCHASE_PAYMENT)
                .where(
                    PURCHASE_PAYMENT.PURCHASE_ID.eq(id)
                )
                .fetch()
                .map(this::mapToPurchasePayment);

        return Optional.of(
            mapToPurchase(
                purchaseRecord,
                items,
                payments
            )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Purchase> findByUserId(UUID userId, PageRequest pageRequest) {
        int offset =
            pageRequest.page() * pageRequest.size();

        List<PurchaseRecord> records =
            dsl.selectFrom(PURCHASE)
                .where(
                    PURCHASE.USER_ID.eq(userId)
                )
                .orderBy(
                    PURCHASE.CREATED_AT.desc(),
                    PURCHASE.ID.desc()
                )
                .limit(pageRequest.size())
                .offset(offset)
                .fetch();

        List<Purchase> purchases =
            records.stream()
                .map(record -> {
                    UUID purchaseId = record.getId();

                    List<PurchaseItem> items =
                        dsl.selectFrom(PURCHASE_ITEM)
                            .where(
                                PURCHASE_ITEM.PURCHASE_ID
                                    .eq(purchaseId)
                            )
                            .fetch()
                            .map(this::mapToPurchaseItem);

                    List<PurchasePayment> payments =
                        dsl.selectFrom(PURCHASE_PAYMENT)
                            .where(
                                PURCHASE_PAYMENT.PURCHASE_ID
                                    .eq(purchaseId)
                            )
                            .fetch()
                            .map(this::mapToPurchasePayment);

                    return mapToPurchase(
                        record,
                        items,
                        payments
                    );
                })
                .toList();

        long totalElements =
            dsl.selectCount()
                .from(PURCHASE)
                .where(
                    PURCHASE.USER_ID.eq(userId)
                )
                .fetchOptional(
                    0,
                    Long.class
                )
                .orElse(0L);

        return new PageResult<>(
            purchases,
            pageRequest.page(),
            pageRequest.size(),
            totalElements
        );
    }

    @Override
    @Transactional
    public Purchase update(Purchase purchase) {

        int currentVersion = purchase.getVersion();
        int newVersion = currentVersion + 1;

        int updatedRows =
            dsl.update(PURCHASE)
                .set(
                    PURCHASE.STATUS,
                    purchase.getStatus().name()
                )
                .set(
                    PURCHASE.EXPECTED_DATE,
                    purchase.getExpectedDate() == null
                        ? null
                        : OffsetDateTime.ofInstant(
                        purchase.getExpectedDate(),
                        ZoneOffset.UTC
                    )
                )
                .set(
                    PURCHASE.PURCHASE_DATE,
                    purchase.getPurchaseDate() == null
                        ? null
                        : OffsetDateTime.ofInstant(
                        purchase.getPurchaseDate(),
                        ZoneOffset.UTC
                    )
                )
                .set(
                    PURCHASE.TOTAL,
                    purchase.getTotal()
                )
                .set(
                    PURCHASE.NOTES,
                    purchase.getNotes()
                )
                .set(
                    PURCHASE.UPDATED_AT,
                    OffsetDateTime.ofInstant(
                        purchase.getUpdatedAt(),
                        ZoneOffset.UTC
                    )
                )
                .set(
                    PURCHASE.VERSION,
                    newVersion
                )
                .where(
                    PURCHASE.ID.eq(purchase.getId())
                        .and(
                            PURCHASE.USER_ID.eq(
                                purchase.getUserId()
                            )
                        )
                        .and(
                            PURCHASE.VERSION.eq(currentVersion)
                        )
                )
                .execute();

        if (updatedRows == 0) {
            throw new OptimisticLockException(
                "Purchase was modified by another transaction"
            );
        }

        Set<UUID> itemIds =
            purchase.getItems()
                .stream()
                .map(PurchaseItem::getId)
                .collect(Collectors.toSet());

        if (itemIds.isEmpty()) {
            dsl.deleteFrom(PURCHASE_ITEM)
                .where(
                    PURCHASE_ITEM.PURCHASE_ID.eq(
                        purchase.getId()
                    )
                )
                .execute();
        } else {
            dsl.deleteFrom(PURCHASE_ITEM)
                .where(
                    PURCHASE_ITEM.PURCHASE_ID.eq(
                            purchase.getId()
                        )
                        .and(
                            PURCHASE_ITEM.ID.notIn(itemIds)
                        )
                )
                .execute();
        }

        for (PurchaseItem item : purchase.getItems()) {
            dsl.insertInto(PURCHASE_ITEM)
                .set(
                    PURCHASE_ITEM.ID,
                    item.getId()
                )
                .set(
                    PURCHASE_ITEM.PURCHASE_ID,
                    purchase.getId()
                )
                .set(
                    PURCHASE_ITEM.CATEGORY_ID,
                    item.getCategoryId()
                )
                .set(
                    PURCHASE_ITEM.NAME,
                    item.getName()
                )
                .set(
                    PURCHASE_ITEM.QUANTITY,
                    item.getQuantity()
                )
                .set(
                    PURCHASE_ITEM.UNIT_PRICE,
                    item.getUnitPrice()
                )
                .onConflict(PURCHASE_ITEM.ID)
                .doUpdate()
                .set(
                    PURCHASE_ITEM.CATEGORY_ID,
                    item.getCategoryId()
                )
                .set(
                    PURCHASE_ITEM.NAME,
                    item.getName()
                )
                .set(
                    PURCHASE_ITEM.QUANTITY,
                    item.getQuantity()
                )
                .set(
                    PURCHASE_ITEM.UNIT_PRICE,
                    item.getUnitPrice()
                )
                .execute();
        }

        Set<UUID> paymentIds =
            purchase.getPayments()
                .stream()
                .map(PurchasePayment::getId)
                .collect(Collectors.toSet());

        if (paymentIds.isEmpty()) {
            dsl.deleteFrom(PURCHASE_PAYMENT)
                .where(
                    PURCHASE_PAYMENT.PURCHASE_ID.eq(
                        purchase.getId()
                    )
                )
                .execute();
        } else {
            dsl.deleteFrom(PURCHASE_PAYMENT)
                .where(
                    PURCHASE_PAYMENT.PURCHASE_ID.eq(
                            purchase.getId()
                        )
                        .and(
                            PURCHASE_PAYMENT.ID.notIn(paymentIds)
                        )
                )
                .execute();
        }

        for (PurchasePayment payment : purchase.getPayments()) {
            dsl.insertInto(PURCHASE_PAYMENT)
                .set(
                    PURCHASE_PAYMENT.ID,
                    payment.getId()
                )
                .set(
                    PURCHASE_PAYMENT.PURCHASE_ID,
                    purchase.getId()
                )
                .set(
                    PURCHASE_PAYMENT.ACCOUNT_ID,
                    payment.getAccountId()
                )
                .set(
                    PURCHASE_PAYMENT.AMOUNT,
                    payment.getAmount()
                )
                .onConflict(PURCHASE_PAYMENT.ID)
                .doUpdate()
                .set(
                    PURCHASE_PAYMENT.ACCOUNT_ID,
                    payment.getAccountId()
                )
                .set(
                    PURCHASE_PAYMENT.AMOUNT,
                    payment.getAmount()
                )
                .execute();
        }

        purchase.incrementVersion();

        return purchase;
    }

    @Override
    @Transactional
    public void delete(Purchase purchase) {
        int deletedRows =
            dsl.deleteFrom(PURCHASE)
                .where(
                    PURCHASE.ID.eq(
                        purchase.getId()
                    )
                )
                .and(
                    PURCHASE.USER_ID.eq(
                        purchase.getUserId()
                    )
                )
                .and(
                    PURCHASE.VERSION.eq(
                        purchase.getVersion()
                    )
                )
                .execute();

        if (deletedRows == 0) {
            throw new OptimisticLockException(
                "Purchase was modified by another transaction"
            );
        }

    }

    private PurchaseItem mapToPurchaseItem(
        PurchaseItemRecord record
    ) {
        return PurchaseItem.restore(
            record.getId(),
            record.getCategoryId(),
            record.getName(),
            record.getQuantity(),
            record.getUnitPrice()
        );
    }

    private PurchasePayment mapToPurchasePayment(
        PurchasePaymentRecord record
    ) {
        return PurchasePayment.restore(
            record.getId(),
            record.getAccountId(),
            record.getAmount()
        );
    }

    private Purchase mapToPurchase(
        PurchaseRecord record,
        List<PurchaseItem> items,
        List<PurchasePayment> payments
    ) {
        return Purchase.restore(
            record.getId(),
            record.getUserId(),
            PurchaseStatus.valueOf(
                record.getStatus()
            ),
            record.getExpectedDate() != null
                ? record.getExpectedDate().toInstant()
                : null,
            record.getPurchaseDate() != null
                ? record.getPurchaseDate().toInstant()
                : null,
            record.getTotal(),
            record.getNotes(),
            record.getCreatedAt().toInstant(),
            record.getUpdatedAt().toInstant(),
            record.getVersion(),
            items,
            payments
        );
    }

}
