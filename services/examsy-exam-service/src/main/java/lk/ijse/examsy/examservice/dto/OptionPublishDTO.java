package lk.ijse.examsy.examservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionPublishDTO {
    @NotBlank(message = "Option text is required.")
    private String optionText;
    private Boolean isCorrect = false;
}
