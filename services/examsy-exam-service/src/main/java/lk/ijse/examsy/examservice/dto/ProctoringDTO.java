package lk.ijse.examsy.examservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProctoringDTO {
    @NotNull(message = "Exam ID is required.")
    private Integer examId;

    @NotBlank(message = "Violation type is required.")
    private String violationType;

    private Integer durationSeconds;
}
