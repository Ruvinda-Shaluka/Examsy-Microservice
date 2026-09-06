package lk.ijse.examsy.notificationservice.kafka;

import lk.ijse.examsy.notificationservice.dto.EmailPayloadDTO;
import lk.ijse.examsy.notificationservice.event.AdminModerationAlertEvent;
import lk.ijse.examsy.notificationservice.service.EmailService;
import lk.ijse.examsy.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminAlertConsumer {

    private final NotificationService notificationService;
    private final EmailService emailService;

    @KafkaListener(topics = "examsy.admin.alert", groupId = "notification-service-group")
    public void consumeAdminAlert(AdminModerationAlertEvent event) {
        log.info("Consumed AdminModerationAlertEvent for recipient: '{}', action: {}",
                event.getRecipientUsername(), event.getActionType());

        try {
            // 1. Dispatch In-App Notification
            notificationService.createNotification(
                    event.getRecipientUsername(),
                    null,
                    event.getRecipientEmail(),
                    event.getTitle(),
                    event.getMessage(),
                    event.getCourseId()
            );

            // 2. Dispatch Transactional Email
            String targetEmail = event.getRecipientEmail() != null && !event.getRecipientEmail().isBlank()
                    ? event.getRecipientEmail()
                    : (event.getRecipientUsername().contains("@")
                        ? event.getRecipientUsername()
                        : event.getRecipientUsername() + "@examsy.mail");

            emailService.sendEmail(EmailPayloadDTO.builder()
                    .to(targetEmail)
                    .recipientName(event.getRecipientUsername())
                    .subject(event.getTitle())
                    .body(event.getMessage())
                    .isHtml(false)
                    .senderAlias("Examsy Administration")
                    .build());

            log.info("Successfully delivered admin alert to recipient '{}'", event.getRecipientUsername());
        } catch (Exception e) {
            log.error("Error processing AdminModerationAlertEvent for {}: {}",
                    event.getRecipientUsername(), e.getMessage(), e);
        }
    }
}
