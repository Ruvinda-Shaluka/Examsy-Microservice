package lk.ijse.examsy.notificationservice.service;

import lk.ijse.examsy.notificationservice.dto.EmailPayloadDTO;

public interface EmailService {

    void sendEmail(EmailPayloadDTO payload);

    void sendGradeReleaseEmail(String toEmail, String studentName, String examTitle, String score, String maxScore, String gradeLetter, String feedback);

    void sendWelcomeEmail(String toEmail, String username, String role);

    void sendExamConfirmationEmail(String toEmail, String studentName, Integer examId, String submittedAt);
}
