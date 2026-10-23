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
    (3, 'EUR', 'Euro', '€', 2),
    (4, 'AED', 'United Arab Emirates Dirham', 'د.إ', 2),
    (5, 'AFN', 'Afghan Afghani', '؋', 2),
    (6, 'ALL', 'Albanian Lek', 'L', 2),
    (7, 'AMD', 'Armenian Dram', '֏', 2),
    (8, 'ANG', 'Netherlands Antillean Guilder', 'ƒ', 2),
    (9, 'AOA', 'Angolan Kwanza', 'Kz', 2),
    (10, 'ARS', 'Argentine Peso', '$', 2),
    (11, 'AUD', 'Australian Dollar', '$', 2),
    (12, 'AWG', 'Aruban Florin', 'ƒ', 2),
    (13, 'AZN', 'Azerbaijani Manat', '₼', 2),
    (14, 'BAM', 'Bosnia and Herzegovina Convertible Mark', 'KM', 2),
    (15, 'BBD', 'Barbados Dollar', '$', 2),
    (16, 'BDT', 'Bangladeshi Taka', '৳', 2),
    (17, 'BGN', 'Bulgarian Lev', 'лв', 2),
    (18, 'BHD', 'Bahraini Dinar', '.د.ب', 3),
    (19, 'BIF', 'Burundian Franc', 'FBu', 0),
    (20, 'BMD', 'Bermudian Dollar', '$', 2),
    (21, 'BND', 'Brunei Dollar', '$', 2),
    (22, 'BOB', 'Bolivian Boliviano', 'Bs.', 2),
    (23, 'BRL', 'Brazilian Real', 'R$', 2),
    (24, 'BSD', 'Bahamian Dollar', '$', 2),
    (25, 'BTN', 'Bhutanese Ngultrum', 'Nu.', 2),
    (26, 'BWP', 'Botswana Pula', 'P', 2),
    (27, 'BYN', 'Belarusian Ruble', 'Br', 2),
    (28, 'BZD', 'Belize Dollar', 'BZ$', 2),
    (29, 'CAD', 'Canadian Dollar', '$', 2),
    (30, 'CDF', 'Congolese Franc', 'FC', 2),
    (31, 'CHF', 'Swiss Franc', 'CHF', 2),
    (32, 'CLP', 'Chilean Peso', '$', 0),
    (33, 'CNY', 'Chinese Yuan', '¥', 2),
    (34, 'COP', 'Colombian Peso', '$', 2),
    (35, 'CRC', 'Costa Rican Colon', '₡', 2),
    (36, 'CUP', 'Cuban Peso', '$', 2),
    (37, 'CVE', 'Cape Verdean Escudo', '$', 2),
    (38, 'CZK', 'Czech Koruna', 'Kč', 2),
    (39, 'DJF', 'Djiboutian Franc', 'Fdj', 0),
    (40, 'DKK', 'Danish Krone', 'kr', 2),
    (41, 'DOP', 'Dominican Peso', 'RD$', 2),
    (42, 'DZD', 'Algerian Dinar', 'د.ج', 2),
    (43, 'EGP', 'Egyptian Pound', '£', 2),
    (44, 'ERN', 'Eritrean Nakfa', 'Nfk', 2),
    (45, 'ETB', 'Ethiopian Birr', 'Br', 2),
    (46, 'FJD', 'Fiji Dollar', '$', 2),
    (47, 'FKP', 'Falkland Islands Pound', '£', 2),
    (48, 'GBP', 'Pound Sterling', '£', 2),
    (49, 'GEL', 'Georgian Lari', '₾', 2),
    (50, 'GHS', 'Ghanaian Cedi', 'GH₵', 2),
    (51, 'GIP', 'Gibraltar Pound', '£', 2),
    (52, 'GMD', 'Gambian Dalasi', 'D', 2),
    (53, 'GNF', 'Guinean Franc', 'FG', 0),
    (54, 'GTQ', 'Guatemalan Quetzal', 'Q', 2),
    (55, 'GYD', 'Guyanese Dollar', '$', 2),
    (56, 'HKD', 'Hong Kong Dollar', '$', 2),
    (57, 'HNL', 'Honduran Lempira', 'L', 2),
    (58, 'HTG', 'Haitian Gourde', 'G', 2),
    (59, 'HUF', 'Hungarian Forint', 'Ft', 2),
    (60, 'IDR', 'Indonesian Rupiah', 'Rp', 2),
    (61, 'ILS', 'Israeli New Shekel', '₪', 2),
    (62, 'INR', 'Indian Rupee', '₹', 2),
    (63, 'IQD', 'Iraqi Dinar', 'ع.د', 3),
    (64, 'IRR', 'Iranian Rial', '﷼', 2),
    (65, 'ISK', 'Icelandic Króna', 'kr', 0),
    (66, 'JMD', 'Jamaican Dollar', 'J$', 2),
    (67, 'JOD', 'Jordanian Dinar', 'د.ا', 3),
    (68, 'JPY', 'Japanese Yen', '¥', 0),
    (69, 'KES', 'Kenyan Shilling', 'KSh', 2),
    (70, 'KGS', 'Kyrgyzstani Som', 'с', 2),
    (71, 'KHR', 'Cambodian Riel', '៛', 2),
    (72, 'KMF', 'Comorian Franc', 'CF', 0),
    (73, 'KPW', 'North Korean Won', '₩', 2),
    (74, 'KRW', 'South Korean Won', '₩', 0),
    (75, 'KWD', 'Kuwaiti Dinar', 'د.ك', 3),
    (76, 'KYD', 'Cayman Islands Dollar', '$', 2),
    (77, 'KZT', 'Kazakhstani Tenge', '₸', 2),
    (78, 'LAK', 'Lao Kip', '₭', 2),
    (79, 'LBP', 'Lebanese Pound', 'ل.ل', 2),
    (80, 'LKR', 'Sri Lankan Rupee', 'Rs', 2),
    (81, 'LRD', 'Liberian Dollar', '$', 2),
    (82, 'LSL', 'Lesotho Loti', 'L', 2),
    (83, 'LYD', 'Libyan Dinar', 'ل.د', 3),
    (84, 'MAD', 'Moroccan Dirham', 'د.م.', 2),
    (85, 'MDL', 'Moldovan Leu', 'L', 2),
    (86, 'MGA', 'Malagasy Ariary', 'Ar', 2),
    (87, 'MKD', 'Macedonian Denar', 'ден', 2),
    (88, 'MMK', 'Myanmar Kyat', 'K', 2),
    (89, 'MNT', 'Mongolian Tögrög', '₮', 2),
    (90, 'MOP', 'Macanese Pataca', 'MOP$', 2),
    (91, 'MRU', 'Mauritanian Ouguiya', 'UM', 2),
    (92, 'MUR', 'Mauritian Rupee', '₨', 2),
    (93, 'MVR', 'Maldivian Rufiyaa', 'Rf', 2),
    (94, 'MWK', 'Malawian Kwacha', 'MK', 2),
    (95, 'MXN', 'Mexican Peso', '$', 2),
    (96, 'MYR', 'Malaysian Ringgit', 'RM', 2),
    (97, 'MZN', 'Mozambican Metical', 'MT', 2),
    (98, 'NAD', 'Namibian Dollar', '$', 2),
    (99, 'NGN', 'Nigerian Naira', '₦', 2),
    (100, 'NIO', 'Nicaraguan Córdoba', 'C$', 2),
    (101, 'NOK', 'Norwegian Krone', 'kr', 2),
    (102, 'NPR', 'Nepalese Rupee', '₨', 2),
    (103, 'NZD', 'New Zealand Dollar', '$', 2),
    (104, 'OMR', 'Omani Rial', 'ر.ع.', 3),
    (105, 'PAB', 'Panamanian Balboa', 'B/.', 2),
    (106, 'PGK', 'Papua New Guinean Kina', 'K', 2),
    (107, 'PHP', 'Philippine Peso', '₱', 2),
    (108, 'PKR', 'Pakistani Rupee', '₨', 2),
    (109, 'PLN', 'Polish Złoty', 'zł', 2),
    (110, 'PYG', 'Paraguayan Guaraní', '₲', 0),
    (111, 'QAR', 'Qatari Riyal', 'ر.ق', 2),
    (112, 'RON', 'Romanian Leu', 'lei', 2),
    (113, 'RSD', 'Serbian Dinar', 'дин.', 2),
    (114, 'RUB', 'Russian Ruble', '₽', 2),
    (115, 'RWF', 'Rwandan Franc', 'FRw', 0),
    (116, 'SAR', 'Saudi Riyal', 'ر.س', 2),
    (117, 'SBD', 'Solomon Islands Dollar', '$', 2),
    (118, 'SCR', 'Seychellois Rupee', '₨', 2),
    (119, 'SDG', 'Sudanese Pound', 'ج.س.', 2),
    (120, 'SEK', 'Swedish Krona', 'kr', 2),
    (121, 'SGD', 'Singapore Dollar', '$', 2),
    (122, 'SHP', 'Saint Helena Pound', '£', 2),
    (123, 'SLE', 'Sierra Leonean Leone', 'Le', 2),
    (124, 'SOS', 'Somali Shilling', 'Sh', 2),
    (125, 'SRD', 'Surinamese Dollar', '$', 2),
    (126, 'SSP', 'South Sudanese Pound', '£', 2),
    (127, 'STN', 'São Tomé and Príncipe Dobra', 'Db', 2),
    (128, 'SVC', 'Salvadoran Colón', '₡', 2),
    (129, 'SYP', 'Syrian Pound', '£', 2),
    (130, 'SZL', 'Swazi Lilangeni', 'L', 2),
    (131, 'THB', 'Thai Baht', '฿', 2),
    (132, 'TJS', 'Tajikistani Somoni', 'ЅМ', 2),
    (133, 'TMT', 'Turkmenistan Manat', 'm', 2),
    (134, 'TND', 'Tunisian Dinar', 'د.ت', 3),
    (135, 'TOP', 'Tongan Paʻanga', 'T$', 2),
    (136, 'TRY', 'Turkish Lira', '₺', 2),
    (137, 'TTD', 'Trinidad and Tobago Dollar', 'TT$', 2),
    (138, 'TWD', 'New Taiwan Dollar', 'NT$', 2),
    (139, 'TZS', 'Tanzanian Shilling', 'Sh', 2),
    (140, 'UAH', 'Ukrainian Hryvnia', '₴', 2),
    (141, 'UGX', 'Ugandan Shilling', 'USh', 0),
    (142, 'UYU', 'Uruguayan Peso', '$U', 2),
    (143, 'UZS', 'Uzbekistani Som', 'so''m', 2),
    (144, 'VES', 'Venezuelan Bolívar Soberano', 'Bs.S', 2),
    (145, 'VND', 'Vietnamese Đồng', '₫', 0),
    (146, 'VUV', 'Vanuatu Vatu', 'VT', 0),
    (147, 'WST', 'Samoan Tala', 'WS$', 2),
    (148, 'XAF', 'Central African CFA Franc', 'FCFA', 0),
    (149, 'XCD', 'East Caribbean Dollar', '$', 2),
    (150, 'XOF', 'West African CFA Franc', 'CFA', 0),
    (151, 'XPF', 'CFP Franc', '₣', 0),
    (152, 'YER', 'Yemeni Rial', '﷼', 2),
    (153, 'ZAR', 'South African Rand', 'R', 2),
    (154, 'ZMW', 'Zambian Kwacha', 'ZK', 2),
    (155, 'ZWL', 'Zimbabwean Dollar', 'Z$', 2);
