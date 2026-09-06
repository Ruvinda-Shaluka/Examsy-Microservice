package lk.ijse.examsy.notificationservice.service.impl;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lk.ijse.examsy.notificationservice.dto.EmailPayloadDTO;
import lk.ijse.examsy.notificationservice.entity.NotificationLog;
import lk.ijse.examsy.notificationservice.repository.NotificationLogRepo;
import lk.ijse.examsy.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final NotificationLogRepo notificationLogRepo;

    @Value("${spring.mail.username:noreply@examsy.com}")
    private String defaultFromEmail;

    @Async
    @Override
    public void sendEmail(EmailPayloadDTO payload) {
        if (payload.getTo() == null || payload.getTo().isBlank()) {
            log.warn("Cannot send email: recipient address is empty.");
            return;
        }

        String status = "SENT";
        String error = null;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String sender = (payload.getSenderAlias() != null && !payload.getSenderAlias().isBlank())
                    ? payload.getSenderAlias() + " (Examsy)"
                    : "Examsy Platform";

            helper.setFrom(new InternetAddress(defaultFromEmail, sender));
            helper.setTo(payload.getTo());
            helper.setSubject(payload.getSubject());
            helper.setText(payload.getBody(), payload.isHtml());

            mailSender.send(message);
            log.info("Email successfully dispatched to: {} with subject: '{}'", payload.getTo(), payload.getSubject());

        } catch (Exception e) {
            status = "FAILED";
            error = e.getMessage();
            log.error("Failed to dispatch email to {}: {}", payload.getTo(), e.getMessage());
        } finally {
            try {
                NotificationLog logEntry = NotificationLog.builder()
                        .recipientEmail(payload.getTo())
                        .recipientUsername(payload.getRecipientName())
                        .subject(payload.getSubject())
                        .channel("EMAIL")
                        .status(status)
                        .errorMessage(error)
                        .sentAt(LocalDateTime.now())
                        .build();
                notificationLogRepo.save(logEntry);
            } catch (Exception ex) {
                log.error("Failed to record notification audit log: {}", ex.getMessage());
            }
        }
    }

    @Override
    public void sendGradeReleaseEmail(String toEmail, String studentName, String examTitle, String score, String maxScore, String gradeLetter, String feedback) {
        String name = studentName != null ? studentName : "Student";
        String subject = "Grade Released: " + examTitle;

        String htmlBody = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 24px; color: #1e293b; }
                        .card { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; border: 1px solid #e2e8f0; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
                        .header { background: linear-gradient(135deg, #4f46e5, #7c3aed); padding: 28px; text-align: center; color: #ffffff; }
                        .content { padding: 32px 24px; }
                        .badge { display: inline-block; padding: 6px 14px; background: #ecfdf5; color: #047857; font-weight: 700; border-radius: 9999px; font-size: 14px; }
                        .score-box { background: #f1f5f9; border-radius: 8px; padding: 20px; margin: 20px 0; text-align: center; }
                        .score { font-size: 32px; font-weight: 800; color: #4f46e5; }
                        .feedback-box { background: #fffbeb; border-left: 4px solid #f59e0b; padding: 16px; margin-top: 16px; border-radius: 4px; font-size: 14px; }
                        .footer { background: #f8fafc; padding: 16px; text-align: center; font-size: 12px; color: #64748b; border-top: 1px solid #e2e8f0; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <div class="header">
                            <h1 style="margin: 0; font-size: 24px;">Examsy Assessment Portal</h1>
                            <p style="margin: 6px 0 0 0; opacity: 0.9;">Official Grade Release Notification</p>
                        </div>
                        <div class="content">
                            <p>Hello <strong>%s</strong>,</p>
                            <p>Your examination results for <strong>%s</strong> have been graded, reviewed, and finalized by your instructor.</p>
                            <div class="score-box">
                                <span class="badge">Final Awarded Grade: %s</span>
                                <div class="score">%s / %s</div>
                            </div>
                            <div class="feedback-box">
                                <strong>Instructor Feedback:</strong><br/>
                                %s
                            </div>
                            <p style="margin-top: 24px;">Log in to your <strong>Examsy Student Dashboard</strong> to review detailed question-by-question analytics and rubric breakdown.</p>
                        </div>
                        <div class="footer">
                            &copy; 2026 Examsy Distributed Assessment Platform. All rights reserved.
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(name, examTitle, gradeLetter != null ? gradeLetter : "N/A", score, maxScore, feedback != null ? feedback : "No specific remarks provided.");

        EmailPayloadDTO payload = EmailPayloadDTO.builder()
                .to(toEmail)
                .recipientName(studentName)
                .subject(subject)
                .body(htmlBody)
                .isHtml(true)
                .senderAlias("Examsy Academics")
                .build();

        sendEmail(payload);
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String username, String role) {
        String subject = "Welcome to Examsy - Your Learning Journey Begins!";
        String roleDisplay = role != null ? role.replace("ROLE_", "") : "Member";

        String htmlBody = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f8fafc; padding: 24px; color: #1e293b; }
                        .card { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; border: 1px solid #e2e8f0; overflow: hidden; }
                        .header { background: #0f172a; padding: 24px; text-align: center; color: #ffffff; }
                        .content { padding: 28px; line-height: 1.6; }
                        .footer { background: #f1f5f9; padding: 14px; text-align: center; font-size: 12px; color: #64748b; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <div class="header">
                            <h2 style="margin: 0;">Welcome to Examsy!</h2>
                        </div>
                        <div class="content">
                            <p>Hi <strong>%s</strong>,</p>
                            <p>Your account has been successfully created with the role of <strong>%s</strong>.</p>
                            <p>With Examsy, you have access to real-time proctored online examinations, automated AI grading, and rich coursework collaboration.</p>
                            <p>Get started by exploring your dashboard and enrolling in classes.</p>
                        </div>
                        <div class="footer">
                            Examsy Platform &bull; Security & Notifications Hub
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(username, roleDisplay);

        EmailPayloadDTO payload = EmailPayloadDTO.builder()
                .to(toEmail)
                .recipientName(username)
                .subject(subject)
                .body(htmlBody)
                .isHtml(true)
                .senderAlias("Examsy Welcome")
                .build();

        sendEmail(payload);
    }

    @Override
    public void sendExamConfirmationEmail(String toEmail, String studentName, Integer examId, String submittedAt) {
        String subject = "Exam Submission Confirmed - Exam #" + examId;
        String body = "Hello " + (studentName != null ? studentName : "Student") + ",\n\n" +
                "This email confirms that your submission for Exam #" + examId + " was successfully recorded on " + submittedAt + ".\n\n" +
                "Your examination has entered the automated grading queue. You will receive an alert once grading is completed.\n\n" +
                "Examsy Assessment Team";

        EmailPayloadDTO payload = EmailPayloadDTO.builder()
                .to(toEmail)
                .recipientName(studentName)
                .subject(subject)
                .body(body)
                .isHtml(false)
                .senderAlias("Examsy Examination")
                .build();

        sendEmail(payload);
    }
}
