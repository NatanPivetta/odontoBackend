--liquibase formatted sql

-- changeset natan.pivetta:20260708-6-1
-- Normaliza numeros de cartao para 8 digitos, preenchendo zeros a esquerda.
-- Se houver valores legados com mais de 8 digitos, preserva os 8 digitos finais.
UPDATE users
SET card_number = RIGHT(LPAD(REGEXP_REPLACE(card_number, '\D', '', 'g'), 8, '0'), 8);

-- changeset natan.pivetta:20260708-6-2
ALTER TABLE users
    ALTER COLUMN card_number TYPE VARCHAR(8);
