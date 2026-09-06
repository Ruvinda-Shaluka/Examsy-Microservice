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
public class VaultExamItemDTO {
    private Integer examId;
    private String title;
    private String examType;
    private BigDecimal score;
    private BigDecimal maxScore;
    private String gradeLetter;
    private LocalDateTime completedAt;
    private String feedback;
}
