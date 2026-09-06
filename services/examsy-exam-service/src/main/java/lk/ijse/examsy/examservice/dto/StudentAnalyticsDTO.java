package lk.ijse.examsy.examservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAnalyticsDTO {
    private Integer totalExamsTaken;
    private BigDecimal averageScorePercentage;
    private Integer totalHonorsReceived;
    private Integer suspiciousEventsCount;
    private BigDecimal integrityScore;
}
