-- ====================================================================
-- Examsy Microservices: Multi-Database Initialization Script
-- Executed automatically by the MySQL container on first startup.
-- Pattern: Database-per-Service (Isolated schemas on a single instance)
-- ====================================================================

-- 1. Auth Service Database (User accounts, roles, auth credentials)
CREATE DATABASE IF NOT EXISTS examsy_auth_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. User Profile Service Database (Student, Teacher, Admin profiles)
CREATE DATABASE IF NOT EXISTS examsy_profile_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 3. Class Service Database (Courses, enrollments, announcements, join requests)
CREATE DATABASE IF NOT EXISTS examsy_class_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 4. Exam Service Database (Exams, questions, options, submissions, proctoring logs)
CREATE DATABASE IF NOT EXISTS examsy_exam_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 5. Grading & AI Service Database (Grading queue, auto-grading results)
CREATE DATABASE IF NOT EXISTS examsy_grading_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 6. Notification Service Database (In-app notifications, logs)
CREATE DATABASE IF NOT EXISTS examsy_notification_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 7. Admin Service Database (Reports, moderation logs)
CREATE DATABASE IF NOT EXISTS examsy_admin_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 8. Analytics Service Database (Denormalized read-models, precomputed metrics)
CREATE DATABASE IF NOT EXISTS examsy_analytics_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
