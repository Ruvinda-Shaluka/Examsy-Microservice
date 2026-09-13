package lk.ijse.examsy.examservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveStudentMonitorDTO {
    // Frontend properties
    private Integer id;
    private String name;
    private String status;
    private Integer flags;
    private Integer totalAwaySeconds;
    private Boolean flagged;
    private List<ProctoringLogDetailDTO> proctoringHistory;

    // Backend metadata
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
