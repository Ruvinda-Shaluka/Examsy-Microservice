-- ====================================================================
-- Examsy Admin Service: V1 Database Migration
-- Target Database: examsy_admin_db
-- ====================================================================

CREATE TABLE IF NOT EXISTS reports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reporter_student_id INT,
    reporter_student_username VARCHAR(100) NOT NULL,
    reporter_student_name VARCHAR(150),
    target_course_id INT NOT NULL,
    target_course_name VARCHAR(150) NOT NULL,
    target_teacher_id INT,
    target_teacher_username VARCHAR(100),
    target_teacher_name VARCHAR(150),
    category VARCHAR(50) NOT NULL,
    priority_level VARCHAR(20) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    admin_notes TEXT,
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    resolved_by_admin_username VARCHAR(100),
    INDEX idx_reports_status (status),
    INDEX idx_reports_target_course (target_course_id),
    INDEX idx_reports_teacher_username (target_teacher_username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS platform_metrics (
    id INT AUTO_INCREMENT PRIMARY KEY,
    metric_key VARCHAR(100) NOT NULL UNIQUE,
    metric_value BIGINT NOT NULL DEFAULT 0,
    description VARCHAR(255),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed default metric counters
INSERT INTO platform_metrics (metric_key, metric_value, description)
VALUES 
    ('TOTAL_STUDENTS', 0, 'Total active student accounts'),
    ('ACTIVE_TEACHERS', 0, 'Total active teacher accounts'),
    ('TOTAL_USERS', 0, 'Total registered platform users'),
    ('PENDING_REPORTS', 0, 'Current pending moderation reports'),
    ('TOTAL_EXAMS_SUBMITTED', 0, 'Lifetime completed exam submissions'),
    ('TOTAL_GRADES_FINALIZED', 0, 'Lifetime graded submissions')
ON DUPLICATE KEY UPDATE metric_key = metric_key;
