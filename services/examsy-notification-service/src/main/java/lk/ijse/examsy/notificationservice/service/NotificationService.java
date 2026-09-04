package lk.ijse.examsy.notificationservice.service;

import lk.ijse.examsy.notificationservice.dto.DirectAlertRequestDTO;
import lk.ijse.examsy.notificationservice.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {

    List<NotificationDTO> getMyNotifications(String username);

    long getUnreadCount(String username);

    void markAsRead(Integer notificationId, String username);

    void markAllAsRead(String username);

    void createNotification(String username, Integer userId, String recipientEmail, String title, String message, Integer courseId);

    void sendDirectAlert(String senderUsername, DirectAlertRequestDTO request);
}
