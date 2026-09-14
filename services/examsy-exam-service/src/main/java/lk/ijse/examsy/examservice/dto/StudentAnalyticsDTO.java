package lk.ijse.examsy.examservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

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

    private BigDecimal gpa;
    private String bestScore;
    private String bestExam;
    private String lowestScore;
    private String lowestExam;
    private String rankText;
    private String rankSubText;
    private List<StudentChartPointDTO> chartData;
}
