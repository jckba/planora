CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE currency
(
    id SMALLINT PRIMARY KEY,

    code CHAR(3) NOT NULL,

    name VARCHAR(60) NOT NULL,

    symbol VARCHAR(8) NOT NULL,

    decimal_places SMALLINT NOT NULL,

    CONSTRAINT uk_currency_code
        UNIQUE (code),

    CONSTRAINT chk_currency_decimal_places
        CHECK (decimal_places BETWEEN 0 AND 4)
);

COMMENT ON TABLE currency IS
'ISO 4217 currency catalog.';

COMMENT ON COLUMN currency.id IS
'Internal identifier.';

COMMENT ON COLUMN currency.code IS
'ISO 4217 alphabetic code.';

COMMENT ON COLUMN currency.name IS
'Currency display name.';

COMMENT ON COLUMN currency.symbol IS
'Currency symbol.';

COMMENT ON COLUMN currency.decimal_places IS
'Number of decimal places used by the currency.';


CREATE TABLE account_type
(
    id SMALLINT PRIMARY KEY,

    code VARCHAR(40) NOT NULL,

    name VARCHAR(60) NOT NULL,

    CONSTRAINT uk_account_type_code
        UNIQUE (code)
);

COMMENT ON TABLE account_type IS
'Catalog of supported account types.';

COMMENT ON COLUMN account_type.id IS
'Internal identifier.';

COMMENT ON COLUMN account_type.code IS
'Stable business identifier.';

COMMENT ON COLUMN account_type.name IS
'Display name.';


INSERT INTO account_type (id, code, name)
VALUES
    (1, 'CASH', 'Cash'),
    (2, 'BANK', 'Bank'),
    (3, 'DIGITAL_WALLET', 'Digital Wallet');


INSERT INTO currency (
    id,
    code,
    name,
    symbol,
    decimal_places
)
VALUES
    (1, 'PEN', 'Peruvian Sol', 'S/', 2),
    (2, 'USD', 'US Dollar', '$', 2),
    (3, 'EUR', 'Euro', '€', 2);

/*
Aquí recomiendo insertar TODO el catálogo ISO-4217.

No solo PEN, USD y EUR.

De esa forma la aplicación nace preparada
para cualquier moneda soportada oficialmente.
*/
