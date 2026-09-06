package lk.ijse.examsy.adminservice.kafka;

import lk.ijse.examsy.adminservice.event.AdminModerationAlertEvent;
import lk.ijse.examsy.adminservice.event.ClassTerminatedEvent;
import lk.ijse.examsy.adminservice.event.TeacherTerminatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminEventProducer {

    public static final String CLASS_TERMINATED_TOPIC = "examsy.class.terminated";
    public static final String TEACHER_TERMINATED_TOPIC = "examsy.teacher.terminated";
    public static final String ADMIN_ALERT_TOPIC = "examsy.admin.alert";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendClassTerminatedEvent(ClassTerminatedEvent event) {
        log.info("Emitting ClassTerminatedEvent for courseId: {}, teacher: {}", event.getCourseId(), event.getTeacherUsername());
        kafkaTemplate.send(CLASS_TERMINATED_TOPIC, event.getCourseId().toString(), event);
    }

    public void sendTeacherTerminatedEvent(TeacherTerminatedEvent event) {
        log.info("Emitting TeacherTerminatedEvent for teacher: {}", event.getTeacherUsername());
        kafkaTemplate.send(TEACHER_TERMINATED_TOPIC, event.getTeacherUsername(), event);
    }

    public void sendAdminModerationAlert(AdminModerationAlertEvent event) {
        log.info("Emitting AdminModerationAlertEvent to recipient: {}, action: {}", event.getRecipientUsername(), event.getActionType());
        kafkaTemplate.send(ADMIN_ALERT_TOPIC, event.getRecipientUsername(), event);
    }
}
