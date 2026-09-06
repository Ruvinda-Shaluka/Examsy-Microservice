package lk.ijse.examsy.notificationservice.repository;

import lk.ijse.examsy.notificationservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Integer> {

    List<Notification> findByUsernameOrderByCreatedAtDesc(String username);

    long countByUsernameAndIsReadFalse(String username);

    List<Notification> findByUsernameAndIsReadFalse(String username);
}
