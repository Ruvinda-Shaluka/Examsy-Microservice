-- ====================================================================
-- Flyway Migration: V2__seed_admin_account.sql
-- Service: examsy-auth-service | Schema: examsy_auth_db
-- Description: Seeds the initial administrator account for platform access.
-- ====================================================================

INSERT INTO user_accounts (username, email, password_hash, role, auth_provider, preferred_theme, is_active)
VALUES (
    'ruvinda.dev',
    'ruvinda.dev@gmail.com',
    '$2a$10$UVHyt8/ePSNyBeG90/qho.Qv0rVyRh2mrMB2J79OuiUi8rfhXltAC',
    'ADMIN',
    'LOCAL',
    'dark',
    TRUE
)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    role = 'ADMIN',
    is_active = TRUE;
