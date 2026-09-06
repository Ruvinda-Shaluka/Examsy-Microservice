package lk.ijse.examsy.notificationservice.repository;

import lk.ijse.examsy.notificationservice.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationLogRepo extends JpaRepository<NotificationLog, Integer> {

    List<NotificationLog> findByRecipientEmailOrderBySentAtDesc(String recipientEmail);
}
