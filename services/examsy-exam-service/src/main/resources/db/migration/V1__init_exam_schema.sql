-- ===================================================================
-- Examsy Exam Service - Database Migration V1
-- Target Database: examsy_exam_db
-- ===================================================================

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
