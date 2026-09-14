-- ====================================================================
-- Examsy Admin Service: Seed Metrics and Moderation Reports
-- Target Database: examsy_admin_db
-- Migration: V2__seed_admin_metrics_and_reports.sql
-- ====================================================================

USE examsy_admin_db;

-- 1. Synchronize Platform Metric Counters to Actual Database Counts
UPDATE platform_metrics SET metric_value = 111 WHERE metric_key = 'TOTAL_STUDENTS';
UPDATE platform_metrics SET metric_value = 15 WHERE metric_key = 'ACTIVE_TEACHERS';
UPDATE platform_metrics SET metric_value = 128 WHERE metric_key = 'TOTAL_USERS';
UPDATE platform_metrics SET metric_value = 5 WHERE metric_key = 'PENDING_REPORTS';
UPDATE platform_metrics SET metric_value = 486 WHERE metric_key = 'TOTAL_EXAMS_SUBMITTED';
UPDATE platform_metrics SET metric_value = 486 WHERE metric_key = 'TOTAL_GRADES_FINALIZED';

-- 2. Seed Realistic Student Moderation Reports
INSERT INTO reports (
    reporter_student_id,
    reporter_student_username,
    reporter_student_name,
    target_course_id,
    target_course_name,
    target_teacher_id,
    target_teacher_username,
    target_teacher_name,
    category,
    priority_level,
    description,
    status,
    reported_at
) VALUES
(
    2,
    'shalukaofficial24',
    'Shaluka Shaluka',
    1,
    'Advanced Algorithms & Complexity',
    4,
    'turing_a',
    'Dr. Alan Turing',
    'HARMFUL CONTENT',
    'HIGH',
    'Inappropriate comments and abusive remarks directed at students during the live complexity analysis discussion session.',
    'PENDING',
    DATE_SUB(NOW(), INTERVAL 2 DAY)
),
(
    3,
    'student_001',
    'Kasun Perera',
    2,
    'Full-Stack Software Architecture',
    5,
    'lovelace_a',
    'Prof. Ada Lovelace',
    'SPAM',
    'MEDIUM',
    'The class stream was repeatedly flooded with promotional links and unauthorized commercial webinar advertisements.',
    'PENDING',
    DATE_SUB(NOW(), INTERVAL 3 DAY)
),
(
    4,
    'student_002',
    'Chamara Silva',
    3,
    'Distributed Systems & Cloud Computing',
    6,
    'liskov_b',
    'Dr. Barbara Liskov',
    'PERSONAL INFO',
    'CRITICAL',
    'Unredacted student contact numbers and private grading sheets were inadvertently uploaded to the public stream attachments.',
    'PENDING',
    DATE_SUB(NOW(), INTERVAL 1 DAY)
),
(
    5,
    'student_003',
    'Nimal Fernando',
    4,
    'Discrete Mathematics & Graph Theory',
    7,
    'knuth_d',
    'Prof. Donald Knuth',
    'COPYRIGHT',
    'LOW',
    'Third-party copyrighted textbook problems uploaded directly as PDF assessments without proper license attribution.',
    'PENDING',
    DATE_SUB(NOW(), INTERVAL 5 DAY)
),
(
    6,
    'student_004',
    'Anura de Silva',
    5,
    'Modern Compiler Construction',
    8,
    'hopper_g',
    'Rear Adm. Grace Hopper',
    'OTHER',
    'MEDIUM',
    'Exam timing was cut short by 30 minutes unexpectedly due to mismatched deadline timezone configuration in the portal.',
    'PENDING',
    DATE_SUB(NOW(), INTERVAL 4 DAY)
);
