package lk.ijse.examsy.examservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProctoringStatsDTO {
    private Integer submissionId;
    private Integer suspiciousEvents;
    private Integer totalTimeAwaySeconds;
    private String proctoringStatus;
}
