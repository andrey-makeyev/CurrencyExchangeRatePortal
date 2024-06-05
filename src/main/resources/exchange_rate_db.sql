DROP INDEX IF EXISTS idx_fx_rates_date_base_currency;
DROP INDEX IF EXISTS idx_currency_code;

DROP TABLE IF EXISTS currency_amount;
DROP TABLE IF EXISTS fx_rates;
DROP TABLE IF EXISTS currency_list;

CREATE TABLE IF NOT EXISTS currency_list
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    currency_code   VARCHAR(3)   NOT NULL,
    currency_name   VARCHAR(255) NOT NULL,
    currency_number INTEGER,
    minor_units     VARCHAR(255),
    UNIQUE (currency_code)
);

CREATE TABLE IF NOT EXISTS fx_rates
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    fx_rate       DECIMAL(20, 10) NOT NULL,
    fx_rate_date  DATE            NOT NULL,
    fx_rate_type  VARCHAR(255)    NOT NULL,
    base_currency VARCHAR(3)      NOT NULL,
    FOREIGN KEY (base_currency) REFERENCES currency_list (currency_code)
);

CREATE TABLE IF NOT EXISTS currency_amount
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    fx_rate_id      BIGINT          NOT NULL,
    target_currency VARCHAR(3)      NOT NULL,
    amount          DECIMAL(20, 10) NOT NULL,
    FOREIGN KEY (fx_rate_id) REFERENCES fx_rates (id),
    FOREIGN KEY (target_currency) REFERENCES currency_list (currency_code)
);

CREATE INDEX idx_currency_code ON currency_list (currency_code);
CREATE INDEX idx_fx_rates_date_base_currency ON fx_rates (fx_rate_date, base_currency);