-- ====================================================================
-- Flyway Migration: V1__create_user_accounts_table.sql
-- Service: examsy-auth-service | Schema: examsy_auth_db
-- Description: Creates the core user_accounts table for authentication.
-- ====================================================================

CREATE TABLE IF NOT EXISTS user_accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    role ENUM('STUDENT', 'TEACHER', 'ADMIN') NOT NULL,
    auth_provider VARCHAR(20) DEFAULT 'LOCAL',
    preferred_theme VARCHAR(10) DEFAULT 'dark',
    is_active BOOLEAN DEFAULT TRUE,
    reset_code VARCHAR(6),
    reset_code_expires_at DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_username (username),
    INDEX idx_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
