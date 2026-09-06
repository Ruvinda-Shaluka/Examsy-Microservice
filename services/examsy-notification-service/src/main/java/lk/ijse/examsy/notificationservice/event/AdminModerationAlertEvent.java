package lk.ijse.examsy.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminModerationAlertEvent implements Serializable {
    private String recipientUsername;
    private String recipientEmail;
    private String title;
    private String message;
    private Integer courseId;
    private String actionType;
    private LocalDateTime timestamp;
}
