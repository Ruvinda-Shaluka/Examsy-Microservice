package lk.ijse.examsy.notificationservice.kafka;

import lk.ijse.examsy.notificationservice.event.ExamSubmittedEvent;
import lk.ijse.examsy.notificationservice.service.EmailService;
import lk.ijse.examsy.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExamSubmittedConsumer {

    private final NotificationService notificationService;
    private final EmailService emailService;

    @KafkaListener(topics = "examsy.exam.submitted", groupId = "notification-service-group")
    public void consumeExamSubmitted(ExamSubmittedEvent event) {
        log.info("Consumed ExamSubmittedEvent for submissionId: {}, examId: {}, student: '{}'",
                event.getSubmissionId(), event.getExamId(), event.getStudentUsername());

        try {
            String title = "Exam Submitted: Exam #" + event.getExamId();
            String message = "Your exam attempt was successfully submitted on "
                    + (event.getSubmittedAt() != null ? event.getSubmittedAt() : "record")
                    + ". It is currently being processed by our grading engine.";

            // 1. In-App Notification
            notificationService.createNotification(
                    event.getStudentUsername(),
                    event.getStudentId(),
                    null,
                    title,
                    message,
                    null
            );

            // 2. Transactional Confirmation Email
            String targetEmail = event.getStudentUsername().contains("@")
                    ? event.getStudentUsername()
                    : event.getStudentUsername() + "@examsy.mail";

            emailService.sendExamConfirmationEmail(
                    targetEmail,
                    event.getStudentUsername(),
                    event.getExamId(),
                    event.getSubmittedAt() != null ? event.getSubmittedAt().toString() : "now"
            );

            log.info("Successfully dispatched submission receipt alert to '{}'", event.getStudentUsername());
        } catch (Exception e) {
            log.error("Error processing ExamSubmittedEvent for submissionId {}: {}", event.getSubmissionId(), e.getMessage(), e);
        }
    }
}
