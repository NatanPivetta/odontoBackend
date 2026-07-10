--liquibase formatted sql

-- changeset natan.pivetta:20260710-7-1
ALTER TABLE turmas
    ADD COLUMN IF NOT EXISTS codigo_turma VARCHAR(30);

-- changeset natan.pivetta:20260710-7-2
UPDATE turmas
SET codigo_turma = COALESCE(NULLIF(TRIM(codigo_turma), ''), LEFT(TRIM(disciplina), 30))
WHERE codigo_turma IS NULL OR TRIM(codigo_turma) = '';

-- changeset natan.pivetta:20260710-7-3
UPDATE turmas
SET disciplina = CASE
    WHEN UPPER(TRIM(disciplina)) IN ('ODO99012', 'CLINICA_ODONTOLOGICA_I', 'CLINICA ODONTOLOGICA I', 'CLÍNICA ODONTOLÓGICA I') THEN 'ODO99012'
    WHEN UPPER(TRIM(disciplina)) IN ('ODO99013', 'CLINICA_ODONTOLOGICA_II', 'CLINICA ODONTOLOGICA II', 'CLÍNICA ODONTOLÓGICA II') THEN 'ODO99013'
    WHEN UPPER(TRIM(disciplina)) IN ('ODO99014', 'CLINICA_ODONTOLOGICA_III', 'CLINICA ODONTOLOGICA III', 'CLÍNICA ODONTOLÓGICA III') THEN 'ODO99014'
    WHEN UPPER(TRIM(disciplina)) IN ('ODO99016', 'CLINICA_ODONTOLOGICA_IV', 'CLINICA ODONTOLOGICA IV', 'CLÍNICA ODONTOLÓGICA IV') THEN 'ODO99016'
    ELSE 'ODO99012'
END;

-- changeset natan.pivetta:20260710-7-4
ALTER TABLE turmas
    ALTER COLUMN codigo_turma SET NOT NULL;

-- changeset natan.pivetta:20260710-7-5
ALTER TABLE turmas
    DROP CONSTRAINT IF EXISTS ck_turmas_disciplina_clinica;

ALTER TABLE turmas
    ADD CONSTRAINT ck_turmas_disciplina_clinica
    CHECK (disciplina IN (
        'ODO99012',
        'ODO99013',
        'ODO99014',
        'ODO99016'
    ));
