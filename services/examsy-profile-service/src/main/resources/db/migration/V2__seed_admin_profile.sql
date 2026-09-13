-- ====================================================================
-- Flyway Migration: V2__seed_admin_profile.sql
-- Service: examsy-profile-service | Schema: examsy_profile_db
-- Description: Seeds the initial administrator profile for platform management.
-- ====================================================================

INSERT INTO admins (user_id, username, email, full_name, role_level)
VALUES (
    COALESCE((SELECT id FROM examsy_auth_db.user_accounts WHERE email = 'ruvinda.dev@gmail.com' LIMIT 1), 99),
    'ruvinda.dev',
    'ruvinda.dev@gmail.com',
    'Ruvinda Shaluka',
    'SUPER_ADMIN'
)
ON DUPLICATE KEY UPDATE
    full_name = VALUES(full_name),
    role_level = 'SUPER_ADMIN';
