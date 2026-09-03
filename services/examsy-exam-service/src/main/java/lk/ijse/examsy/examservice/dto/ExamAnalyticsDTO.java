package lk.ijse.examsy.examservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamAnalyticsDTO {
    private Integer examId;
    private String title;
    private Integer totalParticipants;
    private BigDecimal averageScore;
    private BigDecimal highestScore;
    private BigDecimal lowestScore;
    private BigDecimal passingRate;
    private Map<String, Long> gradeDistribution;
}
