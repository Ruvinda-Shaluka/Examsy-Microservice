package lk.ijse.examsy.gradingservice.kafka;

import lk.ijse.examsy.gradingservice.event.ExamSubmittedEvent;
import lk.ijse.examsy.gradingservice.service.SmartGradingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExamSubmittedConsumer {

    private final SmartGradingService smartGradingService;

    @KafkaListener(topics = "examsy.exam.submitted", groupId = "grading-service-group")
    public void consumeExamSubmitted(ExamSubmittedEvent event) {
        log.info("Received ExamSubmittedEvent via Kafka for submissionId: {}, examId: {}, student: '{}'",
                event.getSubmissionId(), event.getExamId(), event.getStudentUsername());
        try {
            smartGradingService.processKafkaExamSubmission(event);
        } catch (Exception e) {
            log.error("Failed to process ExamSubmittedEvent for submissionId {}: {}", event.getSubmissionId(), e.getMessage(), e);
        }
    }
}
