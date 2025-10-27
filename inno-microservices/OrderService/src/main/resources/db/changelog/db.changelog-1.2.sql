--liquibase formatted sql

--changeset voodzz:1
ALTER TABLE order_items ALTER COLUMN order_id SET NOT NULL;

--changeset voodzz:2
ALTER TABLE order_items ALTER COLUMN item_id SET NOT NULL;
