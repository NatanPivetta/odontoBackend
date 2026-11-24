--liquibase formatted sql

-- changeset natan.pivetta:20260508-1
CREATE TABLE IF NOT EXISTS feedbacks (
    id               BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL,
    texto            TEXT                                 NOT NULL,
    atividade_id     BIGINT                               NOT NULL,
    professor_id     BIGINT                               NOT NULL,
    created_at       TIMESTAMP                            NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP                            NOT NULL DEFAULT NOW(),
    created_by       VARCHAR(150),
    last_modified_by VARCHAR(150),
    CONSTRAINT pk_feedbacks           PRIMARY KEY (id),
    CONSTRAINT fk_feedbacks_atividade FOREIGN KEY (atividade_id) REFERENCES atividades (id),
    CONSTRAINT fk_feedbacks_professor FOREIGN KEY (professor_id) REFERENCES users      (id)
);

-- changeset natan.pivetta:20260508-2
CREATE INDEX IF NOT EXISTS idx_feedbacks_atividade ON feedbacks (atividade_id);

-- changeset natan.pivetta:20260508-3
ALTER TABLE atividades DROP COLUMN IF EXISTS feedback_privado;