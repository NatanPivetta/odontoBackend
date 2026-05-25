--liquibase formatted sql

-- changeset natan.pivetta:20260525-5-1
-- Rastreia se a matrícula do aluno nesta turma está ativa.
-- Ao matricular em nova turma, as matrículas anteriores são marcadas como false.
ALTER TABLE turma_alunos ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
