package lk.ijse.examsy.notificationservice.kafka;

import lk.ijse.examsy.notificationservice.event.GradeReleasedEvent;
import lk.ijse.examsy.notificationservice.service.EmailService;
import lk.ijse.examsy.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GradeReleasedConsumer {

    private final NotificationService notificationService;
    private final EmailService emailService;

    @KafkaListener(topics = "examsy.grade.released", groupId = "notification-service-group")
    public void consumeGradeReleased(GradeReleasedEvent event) {
        log.info("Consumed GradeReleasedEvent for submissionId: {}, examId: {}, student: '{}'",
                event.getSubmissionId(), event.getExamId(), event.getStudentUsername());

        try {
            String title = "Grade Released: Exam #" + event.getExamId();
            String message = "Your exam has been finalized. Final Score: " + event.getFinalScore()
                    + " / " + event.getMaxScore() + " (Grade: " + event.getAwardedGradeLetter() + "). "
                    + (event.getFeedback() != null ? "Feedback: " + event.getFeedback() : "");

            // 1. In-App Notification
            notificationService.createNotification(
                    event.getStudentUsername(),
                    event.getStudentId(),
                    null,
                    title,
                    message,
                    null
            );

            // 2. Transactional Email Dispatch (if studentUsername is email or placeholder)
            String targetEmail = event.getStudentUsername().contains("@")
                    ? event.getStudentUsername()
                    : event.getStudentUsername() + "@examsy.mail";

            emailService.sendGradeReleaseEmail(
                    targetEmail,
                    event.getStudentUsername(),
                    "Exam #" + event.getExamId(),
                    event.getFinalScore() != null ? event.getFinalScore().toString() : "0",
                    event.getMaxScore() != null ? event.getMaxScore().toString() : "100",
                    event.getAwardedGradeLetter(),
                    event.getFeedback()
            );

            log.info("Successfully processed grade released notification and email for student '{}'", event.getStudentUsername());
        } catch (Exception e) {
            log.error("Error processing GradeReleasedEvent for submissionId {}: {}", event.getSubmissionId(), e.getMessage(), e);
        }
    }
}
