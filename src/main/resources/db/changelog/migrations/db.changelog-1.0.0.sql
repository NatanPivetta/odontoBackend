--liquibase formatted sql

-- changeset natan.pivetta:20260410-1
CREATE TABLE IF NOT EXISTS users (
    id            UUID                     NOT NULL,
    name          VARCHAR(150)             NOT NULL,
    email         VARCHAR(150)             NOT NULL,
    card_number   VARCHAR(9)               NOT NULL,
    password_hash VARCHAR(255)             NOT NULL,
    role          VARCHAR(20)              NOT NULL,
    active        BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by       VARCHAR(9),
    last_modified_by VARCHAR(9),
    CONSTRAINT pk_users             PRIMARY KEY (id),
    CONSTRAINT uq_users_email       UNIQUE (email),
    CONSTRAINT uq_users_card_number UNIQUE (card_number)
);

-- changeset natan.pivetta:20260410-2
CREATE TABLE IF NOT EXISTS turmas (
    id               UUID                     NOT NULL,
    disciplina       VARCHAR(20)              NOT NULL,
    name             VARCHAR(100)             NOT NULL,
    semester         VARCHAR(10)              NOT NULL,
    active           BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by       VARCHAR(9),
    last_modified_by VARCHAR(9),
    CONSTRAINT pk_turmas PRIMARY KEY (id)
);

-- changeset natan.pivetta:20260410-3
CREATE TABLE IF NOT EXISTS turma_alunos (
    turma_id UUID NOT NULL,
    aluno_id UUID NOT NULL,
    CONSTRAINT pk_turma_alunos       PRIMARY KEY (turma_id, aluno_id),
    CONSTRAINT fk_turma_alunos_turma FOREIGN KEY (turma_id) REFERENCES turmas (id) ON DELETE CASCADE,
    CONSTRAINT fk_turma_alunos_aluno FOREIGN KEY (aluno_id) REFERENCES users  (id) ON DELETE CASCADE
);

-- changeset natan.pivetta:20260410-4
CREATE INDEX IF NOT EXISTS idx_users_email        ON users        (email);
CREATE INDEX IF NOT EXISTS idx_users_card_number  ON users        (card_number);
CREATE INDEX IF NOT EXISTS idx_turmas_disciplina  ON turmas       (disciplina);
CREATE INDEX IF NOT EXISTS idx_turma_alunos_aluno ON turma_alunos (aluno_id);