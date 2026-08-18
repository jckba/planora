CREATE TABLE purchase
(
    id            UUID PRIMARY KEY,
    user_id       UUID           NOT NULL,
    status        VARCHAR(20)    NOT NULL,
    expected_date TIMESTAMPTZ,
    purchase_date TIMESTAMPTZ,
    total         NUMERIC(19, 4) NOT NULL DEFAULT 0,
    notes         TEXT,
    created_at    TIMESTAMPTZ    NOT NULL,
    updated_at    TIMESTAMPTZ    NOT NULL,
    version       INTEGER        NOT NULL,

    CONSTRAINT fk_purchase_user
        FOREIGN KEY (user_id)
            REFERENCES app_user (id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT chk_purchase_status
        CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED')),

    CONSTRAINT chk_purchase_total
        CHECK (total >= 0)
);

CREATE INDEX idx_purchase_user
    ON purchase (user_id);

CREATE INDEX idx_purchase_status
    ON purchase (status);

CREATE INDEX idx_purchase_date
    ON purchase (purchase_date);

COMMENT
ON TABLE purchase IS
'Represents a planned or completed purchase.';

CREATE TABLE purchase_item
(
    id          UUID PRIMARY KEY,

    purchase_id UUID           NOT NULL,

    category_id UUID           NOT NULL,

    name        VARCHAR(150)   NOT NULL,

    quantity    NUMERIC(19, 4) NOT NULL,

    unit_price  NUMERIC(19, 4) NOT NULL,

    CONSTRAINT fk_purchase_item_purchase
        FOREIGN KEY (purchase_id)
            REFERENCES purchase (id)
            ON DELETE CASCADE
            ON UPDATE RESTRICT,

    CONSTRAINT fk_purchase_item_category
        FOREIGN KEY (category_id)
            REFERENCES category (id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT chk_purchase_item_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_purchase_item_unit_price
        CHECK (unit_price >= 0)
);

CREATE INDEX idx_purchase_item_purchase
    ON purchase_item (purchase_id);

CREATE INDEX idx_purchase_item_category
    ON purchase_item (category_id);

COMMENT
ON TABLE purchase_item IS
'Products included in a purchase.';

CREATE TABLE purchase_payment
(
    id          UUID PRIMARY KEY,

    purchase_id UUID           NOT NULL,

    account_id  UUID           NOT NULL,

    amount      NUMERIC(19, 4) NOT NULL,

    CONSTRAINT fk_purchase_payment_purchase
        FOREIGN KEY (purchase_id)
            REFERENCES purchase (id)
            ON DELETE CASCADE
            ON UPDATE RESTRICT,

    CONSTRAINT fk_purchase_payment_account
        FOREIGN KEY (account_id)
            REFERENCES account (id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT chk_purchase_payment_amount
        CHECK (amount > 0),

    CONSTRAINT uk_purchase_payment_account
        UNIQUE (purchase_id, account_id)

);

CREATE INDEX idx_purchase_expected_date
    ON purchase (expected_date);

CREATE INDEX idx_purchase_payment_purchase
    ON purchase_payment (purchase_id);

CREATE INDEX idx_purchase_payment_account
    ON purchase_payment (account_id);

COMMENT
ON TABLE purchase_payment IS
'Accounts used to pay a purchase.';
