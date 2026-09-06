package lk.ijse.examsy.gradingservice.kafka;

import lk.ijse.examsy.gradingservice.event.GradeReleasedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GradeEventProducer {

    public static final String GRADE_RELEASED_TOPIC = "examsy.grade.released";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishGradeReleased(GradeReleasedEvent event) {
        log.info("Emitting GradeReleasedEvent for submissionId: {} to topic: {}", event.getSubmissionId(), GRADE_RELEASED_TOPIC);
        kafkaTemplate.send(GRADE_RELEASED_TOPIC, event.getSubmissionId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully produced GradeReleasedEvent for submissionId: {}", event.getSubmissionId());
                    } else {
                        log.error("Failed to produce GradeReleasedEvent for submissionId {}: {}", event.getSubmissionId(), ex.getMessage());
                    }
                });
    }
}
