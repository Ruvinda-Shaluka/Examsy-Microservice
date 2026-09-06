package lk.ijse.examsy.adminservice.kafka;

import lk.ijse.examsy.adminservice.event.ClassReportedEvent;
import lk.ijse.examsy.adminservice.event.ExamSubmittedEvent;
import lk.ijse.examsy.adminservice.event.GradeReleasedEvent;
import lk.ijse.examsy.adminservice.event.UserRegisteredEvent;
import lk.ijse.examsy.adminservice.service.AdminDashboardService;
import lk.ijse.examsy.adminservice.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlatformAnalyticsConsumer {

    private final AdminDashboardService adminDashboardService;
    private final AdminReportService adminReportService;

    @KafkaListener(topics = "examsy.user.registered", groupId = "admin-service-group")
    public void consumeUserRegistered(UserRegisteredEvent event) {
        log.info("Ingesting UserRegisteredEvent in analytics: username={}, role={}", event.getUsername(), event.getRole());
        try {
            adminDashboardService.recordUserRegistered(event.getRole());
        } catch (Exception e) {
            log.error("Failed to process UserRegisteredEvent for {}: {}", event.getUsername(), e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "examsy.exam.submitted", groupId = "admin-service-group")
    public void consumeExamSubmitted(ExamSubmittedEvent event) {
        log.info("Ingesting ExamSubmittedEvent in analytics: submissionId={}, student={}", event.getSubmissionId(), event.getStudentUsername());
        try {
            adminDashboardService.recordExamSubmitted();
        } catch (Exception e) {
            log.error("Failed to process ExamSubmittedEvent for {}: {}", event.getSubmissionId(), e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "examsy.grade.released", groupId = "admin-service-group")
    public void consumeGradeReleased(GradeReleasedEvent event) {
        log.info("Ingesting GradeReleasedEvent in analytics: submissionId={}, grade={}", event.getSubmissionId(), event.getAwardedGradeLetter());
        try {
            adminDashboardService.recordGradeReleased();
        } catch (Exception e) {
            log.error("Failed to process GradeReleasedEvent for {}: {}", event.getSubmissionId(), e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "examsy.class.reported", groupId = "admin-service-group")
    public void consumeClassReported(ClassReportedEvent event) {
        log.info("Ingesting ClassReportedEvent in moderation queue: course={}", event.getTargetCourseName());
        try {
            adminReportService.processReportEvent(event);
        } catch (Exception e) {
            log.error("Failed to process ClassReportedEvent for course {}: {}", event.getTargetCourseName(), e.getMessage(), e);
        }
    }
}
