CREATE TABLE orders (
    id           BIGINT         GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      BIGINT         NOT NULL,
    product_id   BIGINT         NOT NULL,
    quantity     INTEGER        DEFAULT 1 NOT NULL,
    total_amount DOUBLE PRECISION NOT NULL,
    status       VARCHAR(50)    DEFAULT 'PENDING' NOT NULL
);
