--liquibase formatted sql

--changeset voodzz:1
CREATE TABLE IF NOT EXISTS users
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(64)         NOT NULL,
    surname    VARCHAR(128)        NOT NULL,
    birth_date DATE                NOT NULL,
    email      VARCHAR(256) UNIQUE NOT NULL
    );

--changeset voodzz:2
CREATE TABLE IF NOT EXISTS card_info
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users (id) ON DELETE CASCADE,
    number          VARCHAR(16) UNIQUE NOT NULL,
    holder          VARCHAR(128)       NOT NULL,
    expiration_date DATE               NOT NULL
    );

--changeset voodz:3
CREATE INDEX IF NOT EXISTS card_info_user_id_idx ON card_info (user_id);

--chageset voodz:4
CREATE INDEX IF NOT EXISTS users_email_idx ON users (email);
