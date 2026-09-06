package lk.ijse.examsy.gradingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveGradeRequestDTO {
    private BigDecimal score;
    private BigDecimal aiScore;
    private String comments;
}
