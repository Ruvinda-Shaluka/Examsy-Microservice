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
public class StudentQuestionDTO {
    private Integer id;
    private String questionText;
    private String questionType;
    private BigDecimal points;
    private Integer orderIndex;
    private List<StudentOptionDTO> options;
}
