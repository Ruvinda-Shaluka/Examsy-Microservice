-- ====================================================================
-- Examsy Microservices: Multi-Database Initialization Script
-- Executed automatically by the MySQL container on first startup.
-- Pattern: Database-per-Service (Isolated schemas on a single instance)
-- ====================================================================

-- --------------------------------------------------------------------
-- 1. Auth Service Database (User accounts, roles, auth credentials)
-- --------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS examsy_auth_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE examsy_auth_db;

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

-- --------------------------------------------------------------------
-- 2. User Profile Service Database (Student, Teacher, Admin profiles)
-- --------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS examsy_profile_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE examsy_profile_db;

CREATE TABLE IF NOT EXISTS students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    profile_picture_url VARCHAR(255),
    student_identification_number VARCHAR(50) UNIQUE,
    date_of_birth DATE,
    gender VARCHAR(20),
    grade VARCHAR(20),
    major VARCHAR(100),
    academic_bio TEXT,
    cumulative_gpa DECIMAL(3, 2) DEFAULT 0.00,
    notify_email BOOLEAN DEFAULT TRUE,
    notify_push BOOLEAN DEFAULT TRUE,
    notify_identity BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS teachers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    profile_picture_url VARCHAR(255),
    instructor_id VARCHAR(50) UNIQUE,
    specialization VARCHAR(100),
    office_location VARCHAR(100),
    professional_bio TEXT,
    notify_email BOOLEAN DEFAULT TRUE,
    notify_push BOOLEAN DEFAULT TRUE,
    notify_security BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS admins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    profile_picture_url VARCHAR(255),
    role_level VARCHAR(50) DEFAULT 'SUPER_ADMIN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 3. Class Service Database (Courses, enrollments, announcements, join requests)
-- --------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS examsy_class_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE examsy_class_db;

CREATE TABLE IF NOT EXISTS classes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    teacher_id INT,
    teacher_username VARCHAR(50) NOT NULL,
    teacher_name VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    section_name VARCHAR(50),
    academic_term VARCHAR(50),
    class_code VARCHAR(20) NOT NULL UNIQUE,
    banner_image_url VARCHAR(255),
    theme_color_hex VARCHAR(7) DEFAULT '#4F46E5',
    is_archived BOOLEAN DEFAULT FALSE,
    class_code_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_teacher_username (teacher_username),
    INDEX idx_class_code (class_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS class_enrollments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    class_id INT NOT NULL,
    student_id INT,
    student_username VARCHAR(50) NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    student_email VARCHAR(100),
    student_profile_picture_url VARCHAR(255),
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_class_student UNIQUE (class_id, student_username),
    CONSTRAINT fk_enrollment_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    INDEX idx_student_username (student_username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS class_join_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    class_id INT NOT NULL,
    student_id INT,
    student_username VARCHAR(50) NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    student_email VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_join_request UNIQUE (class_id, student_username),
    CONSTRAINT fk_join_request_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    INDEX idx_join_status (class_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS class_announcements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    class_id INT NOT NULL,
    author_username VARCHAR(50) NOT NULL,
    author_name VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_announcement_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    INDEX idx_announcement_class (class_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 4. Exam Service Database (Exams, questions, options, submissions, proctoring logs)
-- --------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS examsy_exam_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE examsy_exam_db;

CREATE TABLE IF NOT EXISTS exams (
    id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    teacher_username VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    exam_mode VARCHAR(20) DEFAULT 'REAL_TIME',
    exam_type VARCHAR(20) DEFAULT 'MCQ',
    scheduled_start_time DATETIME,
    deadline_time DATETIME,
    duration_minutes INT NOT NULL,
    pdf_resource_url VARCHAR(255),
    max_score DECIMAL(5,2),
    status VARCHAR(20) DEFAULT 'PUBLISHED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_exam_course (course_id),
    INDEX idx_exam_teacher (teacher_username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    exam_id INT NOT NULL,
    question_text TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL,
    points DECIMAL(5,2),
    order_index INT,
    model_answer TEXT,
    CONSTRAINT fk_question_exam FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS question_options (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question_id INT NOT NULL,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_option_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS exam_submissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    exam_id INT NOT NULL,
    student_id INT,
    student_username VARCHAR(50) NOT NULL,
    student_name VARCHAR(100),
    actual_start_time DATETIME,
    submitted_at DATETIME,
    status VARCHAR(30) DEFAULT 'NOT_STARTED',
    proctoring_status VARCHAR(30) DEFAULT 'SECURE',
    suspicious_event_count INT DEFAULT 0,
    total_time_away_seconds INT DEFAULT 0,
    last_known_action VARCHAR(100),
    calculated_score DECIMAL(5,2),
    final_score DECIMAL(5,2),
    awarded_grade_letter VARCHAR(2),
    pdf_submission_url VARCHAR(255),
    pdf_feedback TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_exam_student UNIQUE (exam_id, student_username),
    CONSTRAINT fk_submission_exam FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    INDEX idx_submission_student (student_username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS submission_answers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    submission_id INT NOT NULL,
    question_id INT NOT NULL,
    answer_text TEXT,
    selected_option_id INT,
    score_awarded DECIMAL(5,2),
    feedback TEXT,
    CONSTRAINT fk_answer_submission FOREIGN KEY (submission_id) REFERENCES exam_submissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS proctoring_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    submission_id INT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    duration_seconds INT,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_proctor_submission FOREIGN KEY (submission_id) REFERENCES exam_submissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 5. Grading & AI Service Database (Grading queue, auto-grading results)
-- --------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS examsy_grading_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE examsy_grading_db;

CREATE TABLE IF NOT EXISTS grading_tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    submission_id INT NOT NULL,
    exam_id INT NOT NULL,
    student_id INT,
    student_username VARCHAR(100) NOT NULL,
    teacher_username VARCHAR(100),
    exam_type VARCHAR(50),
    pdf_submission_url VARCHAR(500),
    extracted_ocr_text LONGTEXT,
    suggested_score DECIMAL(5, 2),
    final_score DECIMAL(5, 2),
    max_score DECIMAL(5, 2),
    awarded_grade_letter VARCHAR(5),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_OCR',
    matched_concepts TEXT,
    missing_concepts TEXT,
    incorrect_parts TEXT,
    comments TEXT,
    confidence VARCHAR(20),
    submitted_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_gt_submission_id (submission_id),
    INDEX idx_gt_exam_id (exam_id),
    INDEX idx_gt_teacher_username (teacher_username),
    INDEX idx_gt_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS mock_exams (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_username VARCHAR(100) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    topic VARCHAR(100) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_me_student_username (student_username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS mock_questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    mock_exam_id INT NOT NULL,
    question_text TEXT NOT NULL,
    option_a VARCHAR(255) NOT NULL,
    option_b VARCHAR(255) NOT NULL,
    option_c VARCHAR(255) NOT NULL,
    option_d VARCHAR(255) NOT NULL,
    correct_option_index INT NOT NULL,
    explanation TEXT,
    INDEX idx_mq_mock_exam_id (mock_exam_id),
    CONSTRAINT fk_mq_mock_exam FOREIGN KEY (mock_exam_id) REFERENCES mock_exams (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 6. Notification Service Database (In-app notifications, logs)
-- --------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS examsy_notification_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE examsy_notification_db;

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

-- --------------------------------------------------------------------
-- 7. Admin Service Database (Reports, moderation logs)
-- --------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS examsy_admin_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE examsy_admin_db;

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

INSERT INTO platform_metrics (metric_key, metric_value, description)
VALUES 
    ('TOTAL_STUDENTS', 0, 'Total active student accounts'),
    ('ACTIVE_TEACHERS', 0, 'Total active teacher accounts'),
    ('TOTAL_USERS', 0, 'Total registered platform users'),
    ('PENDING_REPORTS', 0, 'Current pending moderation reports'),
    ('TOTAL_EXAMS_SUBMITTED', 0, 'Lifetime completed exam submissions'),
    ('TOTAL_GRADES_FINALIZED', 0, 'Lifetime graded submissions')
ON DUPLICATE KEY UPDATE metric_key = metric_key;

-- --------------------------------------------------------------------
-- 8. Analytics Service Database (Denormalized read-models, precomputed metrics)
-- --------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS examsy_analytics_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ====================================================================
-- Application Database User (Principle of Least Privilege)
-- Microservices connect as 'examsy_app', NOT as root.
-- Root account is reserved for container administration only.
-- ====================================================================
CREATE USER IF NOT EXISTS 'examsy_app'@'%' IDENTIFIED BY 'examsy_app_pass';
GRANT ALL PRIVILEGES ON examsy_auth_db.*         TO 'examsy_app'@'%';
GRANT ALL PRIVILEGES ON examsy_profile_db.*      TO 'examsy_app'@'%';
GRANT ALL PRIVILEGES ON examsy_class_db.*        TO 'examsy_app'@'%';
GRANT ALL PRIVILEGES ON examsy_exam_db.*         TO 'examsy_app'@'%';
GRANT ALL PRIVILEGES ON examsy_grading_db.*      TO 'examsy_app'@'%';
GRANT ALL PRIVILEGES ON examsy_notification_db.* TO 'examsy_app'@'%';
GRANT ALL PRIVILEGES ON examsy_admin_db.*        TO 'examsy_app'@'%';
GRANT ALL PRIVILEGES ON examsy_analytics_db.*    TO 'examsy_app'@'%';
FLUSH PRIVILEGES;
