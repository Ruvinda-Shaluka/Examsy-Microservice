package lk.ijse.examsy.classservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinRequestDTO {
    private Integer requestId;
    private Integer studentId;
    private String studentName;
    private String studentEmail;
    private String initial;
    private LocalDateTime requestedAt;
}
