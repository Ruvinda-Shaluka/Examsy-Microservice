package lk.ijse.examsy.examservice.kafka;

import lk.ijse.examsy.examservice.event.ExamSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExamEventProducer {

    public static final String EXAM_SUBMITTED_TOPIC = "examsy.exam.submitted";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishExamSubmitted(ExamSubmittedEvent event) {
        log.info("Emitting ExamSubmittedEvent for submissionId: {} to topic: {}", event.getSubmissionId(), EXAM_SUBMITTED_TOPIC);
        kafkaTemplate.send(EXAM_SUBMITTED_TOPIC, event.getSubmissionId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully produced ExamSubmittedEvent for submissionId: {}", event.getSubmissionId());
                    } else {
                        log.error("Failed to produce ExamSubmittedEvent for submissionId: {}: {}", event.getSubmissionId(), ex.getMessage());
                    }
                });
    }
}
