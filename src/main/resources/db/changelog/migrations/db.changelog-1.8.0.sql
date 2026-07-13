--liquibase formatted sql

-- changeset natan.pivetta:20260713-8-1
CREATE TABLE IF NOT EXISTS roles (
    id          BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL,
    name        VARCHAR(50)                         NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMP                           NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP                           NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uq_roles_name UNIQUE (name)
);

-- changeset natan.pivetta:20260713-8-2
CREATE TABLE IF NOT EXISTS permissions (
    id          BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL,
    name        VARCHAR(80)                         NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMP                           NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP                           NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_permissions PRIMARY KEY (id),
    CONSTRAINT uq_permissions_name UNIQUE (name)
);

-- changeset natan.pivetta:20260713-8-3
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- changeset natan.pivetta:20260713-8-4
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);

-- changeset natan.pivetta:20260713-8-5
INSERT INTO roles (name, description)
VALUES
    ('ADMIN', 'Administrador do sistema'),
    ('PROFESSOR', 'Professor da disciplina'),
    ('ALUNO', 'Aluno da disciplina'),
    ('COORDENADOR', 'Coordenador com visao ampliada de gestao academica'),
    ('MONITOR', 'Aluno monitor com permissoes operacionais especificas')
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description,
    updated_at = NOW();

-- changeset natan.pivetta:20260713-8-6
INSERT INTO permissions (name, description)
VALUES
    ('USER_MANAGE', 'Criar, alterar, ativar e desativar usuarios'),
    ('USER_VIEW', 'Visualizar usuarios'),
    ('PROFESSOR_VIEW', 'Visualizar professores'),
    ('STUDENT_VIEW', 'Visualizar alunos'),
    ('CLASS_MANAGE', 'Criar, alterar e gerenciar turmas'),
    ('CLASS_VIEW', 'Visualizar turmas'),
    ('ACTIVITY_CREATE', 'Criar atividades clinicas'),
    ('ACTIVITY_UPDATE_OWN', 'Alterar atividades proprias'),
    ('ACTIVITY_UPDATE_ANY', 'Alterar atividades de qualquer usuario'),
    ('ACTIVITY_VIEW_OWN', 'Visualizar atividades proprias'),
    ('ACTIVITY_VIEW_ANY', 'Visualizar atividades de qualquer usuario'),
    ('ACTIVITY_REVIEW', 'Revisar atividades clinicas'),
    ('FEEDBACK_CREATE', 'Criar feedbacks'),
    ('FEEDBACK_VIEW_OWN', 'Visualizar feedbacks proprios'),
    ('FEEDBACK_VIEW_ANY', 'Visualizar feedbacks de qualquer usuario')
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description,
    updated_at = NOW();

-- changeset natan.pivetta:20260713-8-7
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON TRUE
WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- changeset natan.pivetta:20260713-8-8
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    'USER_VIEW',
    'USER_MANAGE',
    'PROFESSOR_VIEW',
    'STUDENT_VIEW',
    'CLASS_MANAGE',
    'CLASS_VIEW',
    'PROFESSOR_VIEW',
    'ACTIVITY_CREATE',
    'ACTIVITY_UPDATE_OWN',
    'ACTIVITY_UPDATE_ANY',
    'ACTIVITY_VIEW_OWN',
    'ACTIVITY_VIEW_ANY',
    'ACTIVITY_REVIEW',
    'FEEDBACK_CREATE',
    'FEEDBACK_VIEW_OWN',
    'FEEDBACK_VIEW_ANY'
)
WHERE r.name = 'PROFESSOR'
ON CONFLICT DO NOTHING;

-- changeset natan.pivetta:20260713-8-9
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    'CLASS_VIEW',
    'ACTIVITY_CREATE',
    'ACTIVITY_UPDATE_OWN',
    'ACTIVITY_VIEW_OWN',
    'FEEDBACK_VIEW_OWN'
)
WHERE r.name = 'ALUNO'
ON CONFLICT DO NOTHING;

-- changeset natan.pivetta:20260713-8-10
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    'USER_VIEW',
    'PROFESSOR_VIEW',
    'STUDENT_VIEW',
    'CLASS_MANAGE',
    'CLASS_VIEW',
    'ACTIVITY_VIEW_ANY',
    'FEEDBACK_VIEW_ANY'
)
WHERE r.name = 'COORDENADOR'
ON CONFLICT DO NOTHING;

-- changeset natan.pivetta:20260713-8-11
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    'CLASS_VIEW',
    'PROFESSOR_VIEW',
    'ACTIVITY_CREATE',
    'ACTIVITY_VIEW_ANY',
    'FEEDBACK_VIEW_ANY'
)
WHERE r.name = 'MONITOR'
ON CONFLICT DO NOTHING;

-- changeset natan.pivetta:20260713-8-12
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = CASE
    WHEN LOWER(u.email) = 'admin@odonto.ufrgs.br' THEN 'ADMIN'
    ELSE u.role
END
ON CONFLICT DO NOTHING;

-- changeset natan.pivetta:20260713-8-13
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles (role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission ON role_permissions (permission_id);
