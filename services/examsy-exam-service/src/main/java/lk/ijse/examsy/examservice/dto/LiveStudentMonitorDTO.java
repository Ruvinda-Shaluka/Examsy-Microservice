package lk.ijse.examsy.examservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveStudentMonitorDTO {
    private Integer studentId;
    private String studentName;
    private String studentUsername;
    private String submissionStatus;
    private String proctoringStatus;
    private Integer suspiciousEvents;
    private Integer timeAwaySeconds;
    private String lastAction;
    private LocalDateTime startedAt;
}
