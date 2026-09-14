-- ====================================================================
-- Examsy Notification Microservice: Seed Notification History
-- Database: examsy_notification_db
-- Migration: V2__seed_notification_history.sql
-- ====================================================================

USE examsy_notification_db;

-- 1. Student Notifications for 'shalukaofficial24' / 'shalukaofficial24@gmail.com'
INSERT INTO notifications (user_id, username, recipient_email, title, message, is_read, course_id, created_at) VALUES
(
    2,
    'shalukaofficial24',
    'shalukaofficial24@gmail.com',
    'Grade Released: Midterm: Asymptotic Complexity & Dynamic Programming',
    'Your exam attempt has been graded by Dr. Alan Turing. Final Score: 94 / 100 (Grade: A). Feedback: Exceptional analysis of recurrence relations and memoization tables.',
    FALSE,
    1,
    DATE_SUB(NOW(), INTERVAL 3 HOUR)
),
(
    2,
    'shalukaofficial24',
    'shalukaofficial24@gmail.com',
    'Announcement: Full-Stack Software Architecture',
    'Prof. Ada Lovelace posted: "Take-Home Assignment on Resilient Distributed Architecture Design is now active. Please read the architectural constraints carefully before starting."',
    FALSE,
    2,
    DATE_SUB(NOW(), INTERVAL 12 HOUR)
),
(
    2,
    'shalukaofficial24',
    'shalukaofficial24@gmail.com',
    'New Live Exam: Midterm: Asymptotic Complexity & Dynamic Programming',
    'Dr. Alan Turing has scheduled a new live assessment in Advanced Algorithms & Complexity.',
    TRUE,
    1,
    DATE_SUB(NOW(), INTERVAL 2 DAY)
),
(
    2,
    'shalukaofficial24',
    'shalukaofficial24@gmail.com',
    'Exam Submitted: Midterm: Asymptotic Complexity & Dynamic Programming',
    'Your exam attempt was successfully submitted on record. It has been processed and forwarded to your instructor.',
    TRUE,
    1,
    DATE_SUB(NOW(), INTERVAL 3 DAY)
),
(
    2,
    'shalukaofficial24',
    'shalukaofficial24@gmail.com',
    'Announcement: Advanced Algorithms & Complexity',
    'Dr. Alan Turing posted: "Welcome to ALGO-301! Lecture slides for Asymptotic Complexity and Dynamic Programming proofs have been posted to the stream."',
    TRUE,
    1,
    DATE_SUB(NOW(), INTERVAL 5 DAY)
),
(
    2,
    'shalukaofficial24',
    'shalukaofficial24@gmail.com',
    'Class Join Request Approved: Advanced Algorithms & Complexity',
    'Your request to join ALGO-301 section CS-A was approved by Dr. Alan Turing. You now have access to all classroom assessments and stream announcements.',
    TRUE,
    1,
    DATE_SUB(NOW(), INTERVAL 7 DAY)
),
(
    2,
    'shalukaofficial24',
    'shalukaofficial24@gmail.com',
    'Welcome to Examsy!',
    'Your student account has been successfully initialized. Access your Classrooms Hub, review your Calendar, and verify your Academic Vault.',
    TRUE,
    NULL,
    DATE_SUB(NOW(), INTERVAL 10 DAY)
);

-- 2. Teacher Notifications for 'turing_a' / 'alan.turing@examsy.edu'
INSERT INTO notifications (user_id, username, recipient_email, title, message, is_read, course_id, created_at) VALUES
(
    4,
    'turing_a',
    'alan.turing@examsy.edu',
    'Proctoring Alert: Suspicious Tab Switch Detected',
    'Silent proctoring detected suspicious tab switches during Midterm: Asymptotic Complexity & Dynamic Programming for student STU-10001.',
    FALSE,
    1,
    DATE_SUB(NOW(), INTERVAL 2 HOUR)
),
(
    4,
    'turing_a',
    'alan.turing@examsy.edu',
    'New Submission: Midterm: Asymptotic Complexity & Dynamic Programming',
    'Student Shaluka (STU-10001) has submitted their attempt for Exam #1. MCQ auto-grading completed with a preliminary score of 94/100.',
    FALSE,
    1,
    DATE_SUB(NOW(), INTERVAL 6 HOUR)
),
(
    4,
    'turing_a',
    'alan.turing@examsy.edu',
    'New Class Join Request: Advanced Algorithms & Complexity',
    'Kasun Perera has requested to join your class "Advanced Algorithms & Complexity". Visit the People tab to approve or decline.',
    TRUE,
    1,
    DATE_SUB(NOW(), INTERVAL 1 DAY)
),
(
    4,
    'turing_a',
    'alan.turing@examsy.edu',
    'Announcement Broadcast Confirmation: ALGO-301',
    'Your announcement "Welcome to ALGO-301! Lecture slides for Asymptotic Complexity..." was successfully dispatched to all 28 enrolled students.',
    TRUE,
    1,
    DATE_SUB(NOW(), INTERVAL 5 DAY)
),
(
    4,
    'turing_a',
    'alan.turing@examsy.edu',
    'Classroom Hub Initialized: Advanced Algorithms & Complexity',
    'Class ALGO-301 section CS-A has been created successfully. Class invite code: EX-ALGO301.',
    TRUE,
    1,
    DATE_SUB(NOW(), INTERVAL 8 DAY)
),
(
    4,
    'turing_a',
    'alan.turing@examsy.edu',
    'Welcome to Examsy for Instructors!',
    'Your faculty account is active. You can now configure question banks, launch live proctored assessments, and review real-time student analytics.',
    TRUE,
    NULL,
    DATE_SUB(NOW(), INTERVAL 12 DAY)
);

-- 3. Additional Baseline Notifications for Sample Users
INSERT INTO notifications (user_id, username, recipient_email, title, message, is_read, course_id, created_at) VALUES
(
    5,
    'lovelace_a',
    'ada.lovelace@examsy.edu',
    'New Submission: Take-Home: Resilient Distributed Architecture Design',
    'Student Kasun Perera (STU-10002) has submitted their architectural proof for ARCH-401.',
    FALSE,
    2,
    DATE_SUB(NOW(), INTERVAL 8 HOUR)
),
(
    10,
    'student_001',
    'student001@examsy.edu',
    'Announcement: Advanced Algorithms & Complexity',
    'Dr. Alan Turing posted: "Welcome to ALGO-301! Lecture slides for Asymptotic Complexity and Dynamic Programming proofs have been posted."',
    TRUE,
    1,
    DATE_SUB(NOW(), INTERVAL 4 DAY)
);
