--liquibase formatted sql

--changeset voodzz:1
ALTER TABLE orders ADD COLUMN user_email VARCHAR(256);