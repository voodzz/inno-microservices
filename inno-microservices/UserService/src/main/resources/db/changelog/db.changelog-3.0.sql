--liquibase formatted sql

--changeset voodzz:1
DROP SEQUENCE IF EXISTS users_id_seq CASCADE;