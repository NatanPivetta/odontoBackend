--liquibase formatted sql

-- changeset natan.pivetta:20260708-6-1
-- Normaliza numeros de cartao para 8 digitos, preenchendo zeros a esquerda.
UPDATE users
SET card_number = LPAD(REGEXP_REPLACE(card_number, '\D', '', 'g'), 8, '0')
WHERE LENGTH(REGEXP_REPLACE(card_number, '\D', '', 'g')) <= 8;

-- changeset natan.pivetta:20260708-6-2
-- Atualiza o admin legado de 9 digitos para o novo formato de 8 digitos.
UPDATE users
SET card_number = '00000001'
WHERE card_number = '000000001';

-- changeset natan.pivetta:20260708-6-3
ALTER TABLE users
    ALTER COLUMN card_number TYPE VARCHAR(8);
