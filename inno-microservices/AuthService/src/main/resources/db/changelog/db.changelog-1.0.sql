--liquibase formatted sql

--changeset voodzz:1
CREATE TABLE IF NOT EXISTS users
(
    id       BIGSERIAL PRIMARY KEY,
    username VARCHAR(128) NOT NULL UNIQUE,
    password VARCHAR(64)  NOT NULL
);

--changeset voodzz:2
CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(256) NOT NULL UNIQUE,
    user_id     BIGINT REFERENCES users (id) ON DELETE CASCADE,
    expiry_date timestamptz  NOT NULL
);

--changeset voodzz:3
CREATE TABLE IF NOT EXISTS roles
(
    id      BIGSERIAL PRIMARY KEY,
    role    VARCHAR(128) NOT NULL,
    user_id BIGINT REFERENCES users (id) ON DELETE CASCADE
)