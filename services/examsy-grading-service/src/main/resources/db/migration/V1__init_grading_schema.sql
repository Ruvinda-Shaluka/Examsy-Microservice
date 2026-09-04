-- ====================================================================
-- Examsy Grading & AI Microservice: Flyway Initial Schema Migration
-- Database: examsy_grading_db
-- ====================================================================

-- 1. Grading Tasks (Holds OCR text, AI evaluated rubrics, and teacher review state)
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

-- 2. Mock Exams (Holds AI-generated mock tests requested by students)
CREATE TABLE IF NOT EXISTS mock_exams (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_username VARCHAR(100) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    topic VARCHAR(100) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_me_student_username (student_username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Mock Questions (MCQ question bank generated for each mock test)
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
