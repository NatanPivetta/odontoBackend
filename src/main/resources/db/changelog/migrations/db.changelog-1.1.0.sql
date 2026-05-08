--liquibase formatted sql

-- changeset natan.pivetta:20260504-1
CREATE TABLE IF NOT EXISTS atividades (
    id                       BIGINT  GENERATED ALWAYS AS IDENTITY NOT NULL,
    data                     DATE                                  NOT NULL,
    data_conclusao           DATE,
    prontuario               VARCHAR(20)                           NOT NULL,
    nome_paciente            VARCHAR(150),
    observacoes              TEXT,
    feedback_privado         TEXT,
    status                   VARCHAR(20)                           NOT NULL DEFAULT 'PENDENTE',
    aluno_id                 BIGINT                                NOT NULL,
    professor_orientador_id  BIGINT                                NOT NULL,
    professor_tutor_id       BIGINT,
    turma_id                 BIGINT                                NOT NULL,
    atividade_pai_id         BIGINT,
    created_at               TIMESTAMP                             NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP                             NOT NULL DEFAULT NOW(),
    created_by               VARCHAR(150),
    last_modified_by         VARCHAR(150),
    CONSTRAINT pk_atividades                          PRIMARY KEY (id),
    CONSTRAINT fk_atividades_aluno                    FOREIGN KEY (aluno_id)                REFERENCES users      (id),
    CONSTRAINT fk_atividades_professor_orientador     FOREIGN KEY (professor_orientador_id) REFERENCES users      (id),
    CONSTRAINT fk_atividades_professor_tutor          FOREIGN KEY (professor_tutor_id)      REFERENCES users      (id),
    CONSTRAINT fk_atividades_turma                    FOREIGN KEY (turma_id)                REFERENCES turmas     (id),
    CONSTRAINT fk_atividades_pai                      FOREIGN KEY (atividade_pai_id)        REFERENCES atividades (id)
);

-- changeset natan.pivetta:20260504-2
CREATE INDEX IF NOT EXISTS idx_atividades_aluno       ON atividades (aluno_id);
CREATE INDEX IF NOT EXISTS idx_atividades_turma       ON atividades (turma_id);
CREATE INDEX IF NOT EXISTS idx_atividades_pai         ON atividades (atividade_pai_id);
CREATE INDEX IF NOT EXISTS idx_atividades_status      ON atividades (status);

-- changeset natan.pivetta:20260504-3
ALTER TABLE atividades ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
