--liquibase formatted sql

--changeset voodzz:1
CREATE SEQUENCE IF NOT EXISTS users_id_seq AS BIGINT;

--changeset voodzz:2
SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 0) FROM users) + 1, false);

--changeset voodzz:3
ALTER TABLE users
    ALTER COLUMN id
        SET DEFAULT nextval('users_id_seq'::regclass);

--changeset voodzz:4
ALTER SEQUENCE users_id_seq OWNED BY users.id;