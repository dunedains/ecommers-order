CREATE TABLE orders (
    id           NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      NUMBER(19)     NOT NULL,
    product_id   NUMBER(19)     NOT NULL,
    quantity     NUMBER(10)     DEFAULT 1 NOT NULL,
    total_amount NUMBER(15, 2)  NOT NULL,
    status       VARCHAR2(50)   DEFAULT 'PENDING' NOT NULL
);
