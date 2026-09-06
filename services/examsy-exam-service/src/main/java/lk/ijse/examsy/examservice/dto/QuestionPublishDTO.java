package lk.ijse.examsy.examservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class QuestionPublishDTO {

    @NotBlank(message = "Question text is required.")
    private String questionText;

    @NotBlank(message = "Question type is required.")
    private String questionType;

    @NotNull(message = "Points are required.")
    private BigDecimal points;

    private Integer orderIndex;
    private String modelAnswer;
    private List<OptionPublishDTO> options;
}
