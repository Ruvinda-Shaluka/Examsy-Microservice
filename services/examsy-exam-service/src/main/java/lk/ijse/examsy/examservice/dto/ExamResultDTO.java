package lk.ijse.examsy.examservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResultDTO {
    private Integer submissionId;
    private Integer examId;
    private String status;
    private BigDecimal calculatedScore;
    private BigDecimal maxScore;
    private String awardedGradeLetter;
    private LocalDateTime submittedAt;
    private String message;
}
