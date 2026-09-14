package lk.ijse.examsy.notificationservice.repository;

import lk.ijse.examsy.notificationservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Integer> {

    @Query("SELECT n FROM Notification n WHERE n.username = :identifier OR n.recipientEmail = :identifier ORDER BY n.createdAt DESC")
    List<Notification> findByUserOrEmailOrderByCreatedAtDesc(@Param("identifier") String identifier);

    @Query("SELECT COUNT(n) FROM Notification n WHERE (n.username = :identifier OR n.recipientEmail = :identifier) AND n.isRead = false")
    long countByUserOrEmailAndIsReadFalse(@Param("identifier") String identifier);

    @Query("SELECT n FROM Notification n WHERE (n.username = :identifier OR n.recipientEmail = :identifier) AND n.isRead = false")
    List<Notification> findByUserOrEmailAndIsReadFalse(@Param("identifier") String identifier);

    List<Notification> findByUsernameOrderByCreatedAtDesc(String username);

    long countByUsernameAndIsReadFalse(String username);

    List<Notification> findByUsernameAndIsReadFalse(String username);
}

