package lk.ijse.examsy.notificationservice.service.impl;

import lk.ijse.examsy.notificationservice.dto.DirectAlertRequestDTO;
import lk.ijse.examsy.notificationservice.dto.EmailPayloadDTO;
import lk.ijse.examsy.notificationservice.dto.NotificationDTO;
import lk.ijse.examsy.notificationservice.entity.Notification;
import lk.ijse.examsy.notificationservice.repository.NotificationRepo;
import lk.ijse.examsy.notificationservice.service.EmailService;
import lk.ijse.examsy.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepo notificationRepo;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    @Override
    public List<NotificationDTO> getMyNotifications(String username) {
        log.info("Fetching notifications feed for user '{}'", username);
        return notificationRepo.findByUsernameOrderByCreatedAtDesc(username)
                .stream()
                .map(n -> NotificationDTO.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .isRead(n.getIsRead())
                        .createdAt(n.getCreatedAt())
                        .courseId(n.getCourseId())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public long getUnreadCount(String username) {
        long count = notificationRepo.countByUsernameAndIsReadFalse(username);
        log.info("Unread notifications count for user '{}': {}", username, count);
        return count;
    }

    @Transactional
    @Override
    public void markAsRead(Integer notificationId, String username) {
        log.info("Marking notification ID {} as read for user '{}'", notificationId, username);
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + notificationId));

        if (!notification.getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized: Cannot mark notifications belonging to other users.");
        }

        notification.setIsRead(true);
        notificationRepo.save(notification);
    }

    @Transactional
    @Override
    public void markAllAsRead(String username) {
        log.info("Marking all notifications as read for user '{}'", username);
        List<Notification> unread = notificationRepo.findByUsernameAndIsReadFalse(username);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepo.saveAll(unread);
    }

    @Transactional
    @Override
    public void createNotification(String username, Integer userId, String recipientEmail, String title, String message, Integer courseId) {
        log.info("Persisting in-app notification for username '{}', title: '{}'", username, title);
        Notification notif = Notification.builder()
                .userId(userId)
                .username(username)
                .recipientEmail(recipientEmail)
                .title(title)
                .message(message)
                .isRead(false)
                .courseId(courseId)
                .build();
        notificationRepo.save(notif);
    }

    @Transactional
    @Override
    public void sendDirectAlert(String senderUsername, DirectAlertRequestDTO request) {
        log.info("Direct alert dispatched by '{}' to target '{}'", senderUsername, request.getTargetUsername());

        // 1. In-App Notification
        createNotification(
                request.getTargetUsername(),
                null,
                request.getTargetEmail(),
                request.getTitle(),
                request.getMessage(),
                request.getCourseId()
        );

        // 2. Email Notification (if requested)
        if (Boolean.TRUE.equals(request.getSendEmail()) && request.getTargetEmail() != null && !request.getTargetEmail().isBlank()) {
            EmailPayloadDTO email = EmailPayloadDTO.builder()
                    .to(request.getTargetEmail())
                    .recipientName(request.getTargetUsername())
                    .subject(request.getTitle())
                    .body(request.getMessage())
                    .isHtml(false)
                    .senderAlias(senderUsername)
                    .build();
            emailService.sendEmail(email);
        }
    }
}
