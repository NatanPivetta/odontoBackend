--liquibase formatted sql

-- changeset natan.pivetta:20261116-9-1
-- Repair users created after the initial RBAC backfill, preserving custom profiles.
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = u.role
WHERE NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id)
ON CONFLICT DO NOTHING;