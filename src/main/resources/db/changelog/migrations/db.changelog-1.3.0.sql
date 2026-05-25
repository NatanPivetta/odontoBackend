--liquibase formatted sql

-- changeset natan.pivetta:20260524-1
ALTER TABLE atividades ADD COLUMN IF NOT EXISTS tipo             VARCHAR(50);
ALTER TABLE atividades ADD COLUMN IF NOT EXISTS tipo_descricao  VARCHAR(255);

-- changeset natan.pivetta:20260524-2
CREATE INDEX IF NOT EXISTS idx_atividades_tipo ON atividades (tipo);
