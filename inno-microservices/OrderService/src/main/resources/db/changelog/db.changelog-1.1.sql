--liquibase formatted sql

--chageset voodzz:1
CREATE INDEX IF NOT EXISTS orders_user_id_idx ON orders (user_id);

--chageset voodzz:2
CREATE INDEX IF NOT EXISTS items_name_idx ON items (name);

--chageset voodzz:3
CREATE INDEX IF NOT EXISTS order_items_order_id_idx ON order_items (order_id);

--chageset voodzz:4
CREATE INDEX IF NOT EXISTS order_items_item_id_idx ON order_items (item_id);

--changeset voodzz:5
CREATE INDEX IF NOT EXISTS orders_status_idx ON orders (status);