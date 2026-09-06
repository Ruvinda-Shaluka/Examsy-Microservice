package lk.ijse.examsy.notificationservice.kafka;

import lk.ijse.examsy.notificationservice.event.UserRegisteredEvent;
import lk.ijse.examsy.notificationservice.service.EmailService;
import lk.ijse.examsy.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegisteredConsumer {

    private final NotificationService notificationService;
    private final EmailService emailService;

    @KafkaListener(topics = "examsy.user.registered", groupId = "notification-service-group")
    public void consumeUserRegistered(UserRegisteredEvent event) {
        log.info("Consumed UserRegisteredEvent for username: '{}', email: '{}', role: '{}'",
                event.getUsername(), event.getEmail(), event.getRole());

        try {
            // 1. In-App Notification
            notificationService.createNotification(
                    event.getUsername(),
                    event.getUserId(),
                    event.getEmail(),
                    "Welcome to Examsy!",
                    "Your account has been registered successfully. Explore your dashboard to get started.",
                    null
            );

            // 2. Transactional Welcome Email
            if (event.getEmail() != null && !event.getEmail().isBlank()) {
                emailService.sendWelcomeEmail(event.getEmail(), event.getUsername(), event.getRole());
            }

            log.info("Successfully processed onboarding notification and welcome email for '{}'", event.getUsername());
        } catch (Exception e) {
            log.error("Error processing UserRegisteredEvent for user '{}': {}", event.getUsername(), e.getMessage(), e);
        }
    }
}
