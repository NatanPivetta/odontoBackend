\set ON_ERROR_STOP on
BEGIN;

DO $$
BEGIN
    IF current_database() <> 'odonto_e2e' THEN
        RAISE EXCEPTION 'Seed permitido apenas no banco odonto_e2e';
    END IF;
END
$$;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (name, email, card_number, password_hash, role, active)
VALUES
    ('Professor E2E', 'professor-e2e@example.invalid', '99000001',
     crypt('OdontoE2E-2026', gen_salt('bf')), 'PROFESSOR', true),
    ('Aluno E2E', 'aluno-e2e@example.invalid', '99000002',
     crypt('OdontoE2E-2026', gen_salt('bf')), 'ALUNO', true)
ON CONFLICT (card_number) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    active = true;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = u.role
WHERE u.card_number IN ('99000001', '99000002')
ON CONFLICT DO NOTHING;

COMMIT;
