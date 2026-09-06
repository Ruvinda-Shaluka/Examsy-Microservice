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
public class UserRegisteredEvent implements Serializable {
    private Integer userId;
    private String email;
    private String username;
    private String fullName;
    private String role;
    private LocalDateTime registeredAt;
}
