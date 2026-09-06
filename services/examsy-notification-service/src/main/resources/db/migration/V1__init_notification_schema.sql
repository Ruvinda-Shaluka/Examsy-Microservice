-- ====================================================================
-- Examsy Notification & Alert Microservice: Flyway Initial Schema
-- Database: examsy_notification_db
-- ====================================================================

-- 1. In-App Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    username VARCHAR(100) NOT NULL,
    recipient_email VARCHAR(150),
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    course_id INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_notif_username (username),
    INDEX idx_notif_is_read (is_read),
    INDEX idx_notif_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Delivery Audit Logs (Emails and Broadcast Alerts)
CREATE TABLE IF NOT EXISTS notification_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    recipient_email VARCHAR(150) NOT NULL,
    recipient_username VARCHAR(100),
    subject VARCHAR(255) NOT NULL,
    channel VARCHAR(50) NOT NULL DEFAULT 'EMAIL',
    status VARCHAR(50) NOT NULL DEFAULT 'SENT',
    error_message TEXT,
    sent_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_nl_recipient_email (recipient_email),
    INDEX idx_nl_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
