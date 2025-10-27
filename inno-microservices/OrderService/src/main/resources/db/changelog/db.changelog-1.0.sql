--liquibase formatted sql

--changeset voodzz:1
CREATE TABLE IF NOT EXISTS orders
(
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT      NOT NULL,
    status        VARCHAR(64) NOT NULL,
    creation_date DATE        NOT NULL
);

--changeset voodzz:2
CREATE TABLE IF NOT EXISTS items
(
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(256)   NOT NULL,
    price NUMERIC(10, 2) NOT NULL
);

--changeset voodzz:3
CREATE TABLE IF NOT EXISTS order_items
(
    id       BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders (id) ON DELETE CASCADE,
    item_id  BIGINT REFERENCES items (id),
    quantity INT NOT NULL
);