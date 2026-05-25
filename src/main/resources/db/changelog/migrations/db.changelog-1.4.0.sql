--liquibase formatted sql

-- changeset natan.pivetta:20260524-4-1
CREATE TABLE IF NOT EXISTS email_verification_codes (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(150) NOT NULL,
    code        VARCHAR(6)   NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- changeset natan.pivetta:20260524-4-2
CREATE INDEX IF NOT EXISTS idx_email_verification_email ON email_verification_codes (email);
