CREATE TABLE app_user
(
    id UUID PRIMARY KEY,

    username CITEXT NOT NULL,

    email CITEXT NOT NULL,

    password_hash VARCHAR(255) NOT NULL,

    first_name VARCHAR(100) NOT NULL,

    last_name VARCHAR(100) NOT NULL,

    primary_currency_id SMALLINT NOT NULL,

    password_updated_at TIMESTAMPTZ NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    version INTEGER NOT NULL,

    CONSTRAINT uk_app_user_username
        UNIQUE (username),

    CONSTRAINT uk_app_user_email
        UNIQUE (email),

    CONSTRAINT fk_app_user_primary_currency
        FOREIGN KEY (primary_currency_id)
            REFERENCES currency (id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT
);

CREATE INDEX idx_app_user_primary_currency
    ON app_user(primary_currency_id);

COMMENT ON TABLE app_user IS
'Application users.';



CREATE TABLE category
(
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,

    name CITEXT NOT NULL,

    color VARCHAR(20) NOT NULL,

    icon VARCHAR(100) NOT NULL,

    deleted_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    version INTEGER NOT NULL,

    CONSTRAINT fk_category_user
        FOREIGN KEY (user_id)
            REFERENCES app_user(id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT
);

CREATE INDEX idx_category_user
    ON category(user_id);

CREATE UNIQUE INDEX uk_category_user_name
    ON category(user_id, name)
    WHERE deleted_at IS NULL;

COMMENT ON TABLE category IS
'User defined categories.';



CREATE TABLE account
(
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,

    account_type_id SMALLINT NOT NULL,

    currency_id SMALLINT NOT NULL,

    name CITEXT NOT NULL,

    balance NUMERIC(19,4) NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    version INTEGER NOT NULL,

    CONSTRAINT fk_account_user
        FOREIGN KEY (user_id)
            REFERENCES app_user(id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT fk_account_currency
        FOREIGN KEY (currency_id)
            REFERENCES currency(id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT fk_account_type
        FOREIGN KEY (account_type_id)
            REFERENCES account_type(id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT
);

CREATE INDEX idx_account_user
    ON account(user_id);

CREATE INDEX idx_account_currency
    ON account(currency_id);

CREATE INDEX idx_account_type
    ON account(account_type_id);

CREATE UNIQUE INDEX uk_account_user_name
    ON account(user_id, name);

COMMENT ON TABLE account IS
'User accounts.';
