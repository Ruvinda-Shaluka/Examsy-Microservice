package lk.ijse.examsy.gradingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoGradingResultDTO {
    private Integer suggestedScore;
    private BigDecimal maxScore;
    private List<String> matchedConcepts;
    private List<String> missingConcepts;
    private List<String> incorrectParts;
    private String comments;
    private String confidence;
}
