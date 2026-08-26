CREATE TABLE expense
(
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,

    account_id UUID NOT NULL,

    category_id UUID NOT NULL,

    title VARCHAR(150) NOT NULL,

    description TEXT,

    amount NUMERIC(19,4) NOT NULL,

    expense_date TIMESTAMPTZ NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    deleted_at TIMESTAMPTZ,

    version INTEGER NOT NULL,

    CONSTRAINT fk_expense_user
        FOREIGN KEY (user_id)
            REFERENCES app_user(id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT fk_expense_account
        FOREIGN KEY (account_id)
            REFERENCES account(id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT fk_expense_category
        FOREIGN KEY (category_id)
            REFERENCES category(id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT chk_expense_amount
        CHECK (amount > 0)
);

CREATE INDEX idx_expense_user_active
    ON expense(user_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_expense_account
    ON expense(account_id);

CREATE INDEX idx_expense_category
    ON expense(category_id);

CREATE INDEX idx_expense_date
    ON expense(expense_date);

COMMENT ON TABLE expense IS
'Represents an expense made by the user.';

COMMENT ON COLUMN expense.expense_date IS
'Date and time when the expense occurred.';

COMMENT ON COLUMN expense.deleted_at IS
'Timestamp when the expense was soft deleted. NULL means the expense is active.';
