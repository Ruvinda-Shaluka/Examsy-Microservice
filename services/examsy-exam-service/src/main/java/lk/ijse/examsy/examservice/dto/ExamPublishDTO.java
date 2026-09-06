package lk.ijse.examsy.examservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamPublishDTO {

    @NotEmpty(message = "At least one target class must be selected.")
    private List<Integer> targetClassIds;

    @NotBlank(message = "Exam title is required.")
    private String title;

    @NotBlank(message = "Exam mode is required.")
    private String examMode;

    @NotBlank(message = "Exam type is required.")
    private String examType;

    private LocalDateTime scheduledStartTime;
    private LocalDateTime deadlineTime;

    @NotNull(message = "Duration in minutes is required.")
    private Integer durationMinutes;

    private String pdfResourceUrl;
    private BigDecimal maxScore;

    @Valid
    private List<QuestionPublishDTO> questions;
}
