--liquibase formatted sql

--changeset voodzz:1
INSERT INTO users (name, surname, birth_date, email)
VALUES ('Ivan', 'Ivanov', '1970-01-01', 'ivan@emaple.com'),
       ('Petr', 'Petrov', '1980-02-02', 'petr@emaple.com'),
       ('Victoria', 'Victorova', '1990-03-03', 'vic@emaple.com');

--changeset voodzz:2
INSERT INTO card_info (user_id, number, holder, expiration_date)
VALUES (1, '111', 'Ivan Ivanov', '2025-09-20'),
       (2, '222', 'Petr Petrov', '2025-10-20'),
       (3, '333', 'Victoria Victorova', '2025-11-20'),
       (1, '444', 'Ivan Ivanov', '2025-09-25');